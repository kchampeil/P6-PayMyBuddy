package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.TransactionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.TransactionDTO;
import com.paymybuddy.webapp.model.Relationship;
import com.paymybuddy.webapp.model.Transaction;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.RelationshipRepository;
import com.paymybuddy.webapp.repository.TransactionRepository;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.contract.ITransactionService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class TransactionService implements ITransactionService {

    private final TransactionRepository transactionRepository;

    private final RelationshipRepository relationshipRepository;

    private final UserRepository userRepository;

    @Autowired
    TransactionService(TransactionRepository transactionRepository,
                       RelationshipRepository relationshipRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.relationshipRepository = relationshipRepository;
        this.userRepository = userRepository;
    }


    /**
     * opère un transfert entre le compte utilisateur et le compte d'un utilisateur déclaré comme "ami" dans PMB
     *
     * @param transactionDTOToCreate contient les informations sur le mouvement à opérer
     * @return objet TransactionDTO contenant la transaction entre amis créée
     * @throws PMBException si la relation n'existe pas entre les deux utilisateurs
     *                      ou que des données sont manquantes
     */
    @Override
    public Optional<TransactionDTO> transferToFriend(TransactionDTO transactionDTOToCreate) throws PMBException {

        Optional<TransactionDTO> createdTransactionDTO;

        //vérifie qu il ne manque pas d informations
        if (transactionDTOToCreate.isValid()) {

            //vérifie que la relation entre utilisateurs existe bien
            Optional<Relationship> relationship = relationshipRepository.findById(transactionDTOToCreate.getRelationshipId());
            if (relationship.isPresent()) {

                //calcule le montant des frais de transaction à appliquer
                BigDecimal feeAmountToAdd = transactionDTOToCreate.getAmountFeeExcluded()
                        .multiply(TransactionConstants.FEE_PERCENTAGE)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                transactionDTOToCreate.setFeeAmount(feeAmountToAdd);

                //vérifie que le compte de l'utilisateur est suffisamment créditeur pour couvrir la transaction et les frais
                User user = relationship.get().getUser();
                if (user.getBalance()
                        .compareTo(transactionDTOToCreate.getAmountFeeExcluded()
                                .add(feeAmountToAdd)) >= 0) {

                    ModelMapper modelMapper = new ModelMapper();
                    try {
                        //diminue la balance du user du montant de la transaction + frais
                        user.setBalance(user.getBalance()
                                .subtract(transactionDTOToCreate.getAmountFeeExcluded()
                                        .add(transactionDTOToCreate.getFeeAmount())));

                        //augmente la balance du user "ami" du montant de la transaction seulement
                        User friend = relationship.get().getFriend();
                        friend.setBalance(friend.getBalance()
                                .add(transactionDTOToCreate.getAmountFeeExcluded()));

                        //mappe le DTO dans le DAO et indique le type de transfert
                        Transaction transactionToCreate = modelMapper.map(transactionDTOToCreate, Transaction.class);
                        transactionToCreate.setRelationship(relationship.get());
                        transactionToCreate.setFeeBilled(false);

                        // puis le compte utilisateur, le compte ami et la nouvelle transaction sont sauvegardés en base
                        userRepository.save(user);
                        userRepository.save(friend);
                        Transaction createdTransaction = transactionRepository.save(transactionToCreate);

                        // avant mappage inverse du DAO dans le DTO
                        createdTransactionDTO =
                                Optional.ofNullable(modelMapper.map((createdTransaction), TransactionDTO.class));

                        log.info(LogConstants.CREATE_TRANSACTION_OK + transactionDTOToCreate.getTransactionId());

                    } catch (Exception exception) {
                        log.error(LogConstants.CREATE_TRANSACTION_ERROR + transactionDTOToCreate.getTransactionId()
                                + " // " + transactionDTOToCreate.getDate()
                                + " // " + transactionDTOToCreate.getDescription()
                                + " // " + transactionDTOToCreate.getRelationshipId());
                        throw exception;
                    }
                } else {
                    log.error(LogConstants.CREATE_TRANSACTION_ERROR
                            + PMBExceptionConstants.INSUFFICIENT_BALANCE
                            + user.getUserId()
                            + " // " + user.getBalance()
                            + " // " + transactionDTOToCreate.getAmountFeeExcluded()
                            + " // " + transactionDTOToCreate.getFeeAmount());
                    throw new PMBException(PMBExceptionConstants.INSUFFICIENT_BALANCE + user.getUserId());
                }

            } else {
                log.error(LogConstants.CREATE_TRANSACTION_ERROR
                        + PMBExceptionConstants.DOES_NOT_EXISTS_RELATIONSHIP
                        + transactionDTOToCreate.getRelationshipId());
                throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_RELATIONSHIP
                        + transactionDTOToCreate.getRelationshipId());

            }

        } else {
            log.error(LogConstants.CREATE_TRANSACTION_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_NEW_TRANSACTION
                    + "for: " + transactionDTOToCreate.getRelationshipId()
                    + " // " + transactionDTOToCreate.getDate()
                    + " // " + transactionDTOToCreate.getDescription());
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_TRANSACTION);
        }

        return createdTransactionDTO;
    }


    /**
     * récupération de la liste de toutes les transactions d'un utilisateur donné
     * TODO : pour l'instant la liste ne récupère que les transactions où l'utilisateur est le payeur.
     * TODO (suite) Les transactions dont il est bénéficiaire doivent être ajoutées
     *
     * @param userId id de l utilisateur dont on souhaite à récupérer la liste des transactions
     * @return la liste des transactions (DTO)
     */
    @Override
    public List<TransactionDTO> getAllTransactionsForUser(Long userId) throws PMBException {
        List<TransactionDTO> transactionDTOList = new ArrayList<>();

        if (userId != null) {
            Optional<User> user = userRepository.findById(userId);

            //vérifie que l'utilisateur existe
            if (user.isPresent()) {
                List<Transaction> transactionList = transactionRepository.findAllByRelationship_User_UserId(userId);
                ModelMapper modelMapper = new ModelMapper();
                transactionList.forEach(transaction ->
                        transactionDTOList
                                .add(modelMapper.map(transaction, TransactionDTO.class)));
                log.info(LogConstants.LIST_TRANSACTION_OK + transactionDTOList.size());

            } else {
                log.error(LogConstants.LIST_TRANSACTION_ERROR
                        + PMBExceptionConstants.DOES_NOT_EXISTS_USER + userId);
                throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_USER + userId);
            }

        } else {
            log.error(LogConstants.LIST_TRANSACTION_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_LIST_TRANSACTION);
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_LIST_TRANSACTION);
        }

        return transactionDTOList;
    }
}
