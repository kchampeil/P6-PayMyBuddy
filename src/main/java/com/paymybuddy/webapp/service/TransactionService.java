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
import com.paymybuddy.webapp.util.DateUtil;
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
    public TransactionService(TransactionRepository transactionRepository,
                              RelationshipRepository relationshipRepository, UserRepository userRepository) {
        this.transactionRepository = transactionRepository;
        this.relationshipRepository = relationshipRepository;
        this.userRepository = userRepository;
    }

    private static final DateUtil dateUtil = new DateUtil();

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

        Optional<TransactionDTO> createdTransactionDTO = Optional.empty();

        Optional<Relationship> relationship = relationshipRepository.findById(transactionDTOToCreate.getRelationshipId());

        //calcule le montant des frais de transaction à appliquer
        BigDecimal feeAmountToAdd = transactionDTOToCreate.getAmountFeeExcluded()
                .multiply(TransactionConstants.FEE_PERCENTAGE)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        transactionDTOToCreate.setFeeAmount(feeAmountToAdd);

        if (checksBeforeCreatingTransaction(transactionDTOToCreate, relationship)) {
            User user = relationship.get().getUser();

            ModelMapper modelMapper = new ModelMapper();

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
            transactionToCreate.setDate(dateUtil.getCurrentLocalDateTime());
            transactionToCreate.setRelationship(relationship.get());
            transactionToCreate.setFeeBilled(false);

            Transaction createdTransaction;
            try {
                // puis le compte utilisateur, le compte ami et la nouvelle transaction sont sauvegardés en base
                userRepository.save(user);
                userRepository.save(friend);
                createdTransaction = transactionRepository.save(transactionToCreate);

            } catch (Exception exception) {
                log.error(LogConstants.CREATE_TRANSACTION_ERROR + transactionDTOToCreate.getTransactionId()
                        + " // " + transactionDTOToCreate.getDate()
                        + " // " + transactionDTOToCreate.getDescription()
                        + " // " + transactionDTOToCreate.getRelationshipId());
                throw exception;
            }

            // avant mappage inverse du DAO dans le DTO
            createdTransactionDTO =
                    Optional.ofNullable(modelMapper.map(createdTransaction, TransactionDTO.class));

            log.info(LogConstants.CREATE_TRANSACTION_OK + createdTransactionDTO.orElse(null).getTransactionId());
        }
        return createdTransactionDTO;
    }


    /**
     * récupération de la liste de toutes les transactions d'un utilisateur donné
     * TODO V2 : pour l'instant la liste ne récupère que les transactions où l'utilisateur est le payeur.
     * TODO (suite) Les transactions dont il est bénéficiaire pourront être ajoutées dans la prochaine version
     *
     * @param userId id de l utilisateur dont on souhaite à récupérer la liste des transactions
     * @return la liste des transactions (DTO)
     */
    @Override
    public List<TransactionDTO> getAllTransactionsForUser(Long userId) throws PMBException {
        List<TransactionDTO> transactionDTOList = new ArrayList<>();

        if (checksBeforeGettingTransactions(userId)) {
            List<Transaction> transactionList = transactionRepository.findAllByRelationship_User_UserIdOrderByDateDesc(userId);
            transactionList.forEach(transaction ->
                    transactionDTOList
                            .add(mapTransactionToTransactionDTO(transaction)));

            log.info(LogConstants.LIST_TRANSACTION_OK + transactionDTOList.size());
        }

        return transactionDTOList;
    }


    /**
     * vérifie que les informations transmises sont correctes en amont de la création de la transaction
     *
     * @param transactionDTOToCreate contient les informations sur le transfert bancaire à créer
     * @param relationship           relation entre utilisateurs en lien avec le transfert
     * @return true si tout est correct
     * @throws PMBException si des données sont manquantes
     *                      ou que la relation entre utilisateurs n'existe pas
     *                      ou que le compte utilisateur n'est pas suffisamment créditeur
     *                      pour couvrir la transaction et les frais
     */
    private boolean checksBeforeCreatingTransaction(TransactionDTO transactionDTOToCreate,
                                                    Optional<Relationship> relationship) throws PMBException {
        //vérifie qu il ne manque pas d informations
        if (!transactionDTOToCreate.isValid()) {
            log.error(LogConstants.CREATE_TRANSACTION_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_NEW_TRANSACTION
                    + " for: " + transactionDTOToCreate.getRelationshipId()
                    + " // " + transactionDTOToCreate.getDescription());
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_TRANSACTION);
        }

        //vérifie que la relation entre utilisateurs existe bien
        if (!relationship.isPresent()) {
            log.error(LogConstants.CREATE_TRANSACTION_ERROR
                    + PMBExceptionConstants.DOES_NOT_EXISTS_RELATIONSHIP + " for: "
                    + transactionDTOToCreate.getRelationshipId());
            throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_RELATIONSHIP);
        }

        //vérifie que le compte de l'utilisateur est suffisamment créditeur pour couvrir la transaction et les frais
        if (relationship.get().getUser().getBalance()
                .compareTo(transactionDTOToCreate.getAmountFeeExcluded()
                        .add(transactionDTOToCreate.getFeeAmount())) < 0) {

            log.error(LogConstants.CREATE_TRANSACTION_ERROR
                    + PMBExceptionConstants.INSUFFICIENT_BALANCE + " for: "
                    + relationship.get().getUser().getUserId());
            throw new PMBException(PMBExceptionConstants.INSUFFICIENT_BALANCE);
        }

        return true;
    }


    /**
     * vérifie que les informations transmises sont correctes en amont de la récupération de la liste des transactions
     *
     * @param userId identifiant de l'utilisateur pour lequel on souhaite récupérer la liste des transactions
     * @return true si tout est correct
     * @throws PMBException si l'identifiant transmis est nul
     *                      ou que l'utilisateur n'existe pas
     */
    private boolean checksBeforeGettingTransactions(Long userId) throws PMBException {
        //vérifie qu il ne manque pas d informations
        if (userId == null) {
            log.error(LogConstants.LIST_TRANSACTION_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_LIST_TRANSACTION);
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_LIST_TRANSACTION);
        }

        Optional<User> user = userRepository.findById(userId);
        //vérifie que l'utilisateur existe
        if (!user.isPresent()) {
            log.error(LogConstants.LIST_TRANSACTION_ERROR
                    + PMBExceptionConstants.DOES_NOT_EXISTS_USER + " for: " + userId);
            throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_USER);
        }

        return true;
    }


    /**
     * mappe les informations Transaction et Relationship vers TransactionDTO
     *
     * @param transaction transaction information to be mapped to personInfoDTO
     * @return a TransactionDTO
     */
    private TransactionDTO mapTransactionToTransactionDTO(Transaction transaction) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.typeMap(Transaction.class, TransactionDTO.class).addMappings(mapper -> {
            mapper.map(src -> src.getRelationship().getFriend().getFirstname(), TransactionDTO::setFriendFirstname);
            mapper.map(src -> src.getRelationship().getFriend().getLastname(), TransactionDTO::setFriendLastname);
        });

        return modelMapper.map(transaction, TransactionDTO.class);
    }
}
