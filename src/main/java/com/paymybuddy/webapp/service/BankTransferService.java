package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.BankAccount;
import com.paymybuddy.webapp.model.BankTransfer;
import com.paymybuddy.webapp.model.DTO.BankTransferDTO;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.BankAccountRepository;
import com.paymybuddy.webapp.repository.BankTransferRepository;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.contract.IBankTransferService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class BankTransferService implements IBankTransferService {

    private final BankTransferRepository bankTransferRepository;

    private final BankAccountRepository bankAccountRepository;

    private final UserRepository userRepository;

    @Autowired
    BankTransferService(BankTransferRepository bankTransferRepository,
                        BankAccountRepository bankAccountRepository, UserRepository userRepository) {
        this.bankTransferRepository = bankTransferRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }

    /**
     * opère un transfert entre le compte utilisateur et le compte bancaire indiqués
     * selon le sens mentionné dans l'attribut 'type'
     * NB : on ne se soucie pas de l'accord bancaire lors du transfert banque -> utilisateur (hypothèse d'une validation en amont)
     *
     * @param bankTransferDTOToCreate contient les informations sur le mouvement à opérer
     * @return objet bankTransferDTO contenant le transfert bancaire créé
     * @throws PMBException si le compte bancaire indiqué n existe pas
     *                      ou que le compte utilisateur n'est pas suffisamment créditeur (dans le cas d'un DEBIT)
     *                      ou que des données sont manquantes
     */
    @Override
    public Optional<BankTransferDTO> transferWithBankAccount(BankTransferDTO bankTransferDTOToCreate) throws PMBException {
        Optional<BankTransferDTO> createdBankTransferDTO;

        //vérifie qu il ne manque pas d informations
        if (bankTransferDTOToCreate.isValid()) {

            //vérifie que le type de transfert est valide
            if (bankTransferDTOToCreate.typeIsValid()) {

                //vérifie que le compte bancaire associé existe bien
                Optional<BankAccount> bankAccount = bankAccountRepository.findById(bankTransferDTOToCreate.getBankAccountId());
                if (bankAccount.isPresent()) {

                    User user = bankAccount.get().getUser();
                    ModelMapper modelMapper = new ModelMapper();
                    switch (bankTransferDTOToCreate.getType()) {
                        case CREDIT: {
                            //augmente la balance du compte utilisateur du montant du transfert bancaire
                            user.setBalance(user.getBalance().add(bankTransferDTOToCreate.getAmount()));
                            break;
                        }
                        case DEBIT: {
                            //vérifie que le compte utilisateur est suffisamment créditeur
                            //et diminue la balance du user du montant du transfert bancaire
                            if (user.getBalance().compareTo(bankTransferDTOToCreate.getAmount()) >= 0) {
                                user.setBalance(user.getBalance().subtract(bankTransferDTOToCreate.getAmount()));
                            } else {
                                log.error(LogConstants.CREATE_BANK_TRANSFER_ERROR
                                        + PMBExceptionConstants.INSUFFICIENT_BALANCE + user.getUserId() + " // " + user.getBalance()
                                        + " // " + bankTransferDTOToCreate.getAmount());
                                throw new PMBException(PMBExceptionConstants.INSUFFICIENT_BALANCE + user.getUserId());
                            }
                            break;
                        }
                    }

                    //mappe le DTO dans le DAO et associe le compte bancaire au transfert
                    BankTransfer bankTransferToCreate = modelMapper.map(bankTransferDTOToCreate, BankTransfer.class);
                    bankTransferToCreate.setBankAccount(bankAccount.get());

                    // puis le compte client et le nouveau transfert bancaire sont sauvegardés en base
                    BankTransfer createdBankTransfer;
                    try {
                        userRepository.save(user);
                        createdBankTransfer = bankTransferRepository.save(bankTransferToCreate);
                    } catch (Exception exception) {
                        log.error(LogConstants.CREATE_BANK_TRANSFER_ERROR + bankTransferDTOToCreate.getBankAccountId()
                                + " // " + bankTransferDTOToCreate.getDate()
                                + " // " + bankTransferDTOToCreate.getDescription());
                        throw exception;
                    }

                    // avant mappage inverse du DAO dans le DTO
                    createdBankTransferDTO =
                            Optional.ofNullable(modelMapper.map((createdBankTransfer), BankTransferDTO.class));

                    log.info(LogConstants.CREATE_BANK_TRANSFER_OK + bankTransferDTOToCreate.getBankAccountId()
                            + " // " + bankTransferDTOToCreate.getDate()
                            + " // " + bankTransferDTOToCreate.getDescription()
                            + " // " + createdBankTransfer.getBankAccount().getUser().getUserId()
                            + " // " + createdBankTransfer.getType());

                } else {
                    log.error(LogConstants.CREATE_BANK_TRANSFER_ERROR
                            + PMBExceptionConstants.DOES_NOT_EXISTS_BANK_ACCOUNT
                            + bankTransferDTOToCreate.getBankAccountId());
                    throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_BANK_ACCOUNT
                            + bankTransferDTOToCreate.getBankAccountId());

                }
            } else {
                log.error(LogConstants.CREATE_BANK_TRANSFER_ERROR
                        + PMBExceptionConstants.INVALID_BANK_TRANSFER_TYPE + bankTransferDTOToCreate.getBankAccountId());
                throw new PMBException(PMBExceptionConstants.INVALID_BANK_TRANSFER_TYPE + bankTransferDTOToCreate.getType());
            }

        } else {
            log.error(LogConstants.CREATE_BANK_TRANSFER_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_TRANSFER
                    + "for: " + bankTransferDTOToCreate.getBankAccountId()
                    + " // " + bankTransferDTOToCreate.getDate()
                    + " // " + bankTransferDTOToCreate.getDescription());
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_TRANSFER);
        }

        return createdBankTransferDTO;
    }


    /**
     * récupération de la liste de tous les transferts bancaires d un utilisateur donné
     *
     * @param userId id de l utilisateur dont on souhaite à récupérer la liste des transferts bancaires
     * @return la liste des transferts bancaires (DTO)
     */
    @Override
    public List<BankTransferDTO> getAllBankTransfersForUser(Long userId) throws PMBException {
        List<BankTransferDTO> bankTransferDTOList = new ArrayList<>();

        if (userId != null) {
            Optional<User> user = userRepository.findById(userId);

            //vérifie que l'utilisateur existe
            if (user.isPresent()) {
                List<BankTransfer> bankTransferList = bankTransferRepository.findAllByBankAccount_User_UserId(userId);
                ModelMapper modelMapper = new ModelMapper();
                bankTransferList.forEach(bankTransfer ->
                        bankTransferDTOList
                                .add(modelMapper.map(bankTransfer, BankTransferDTO.class)));

            } else {
                log.error(LogConstants.LIST_BANK_TRANSFER_ERROR
                        + PMBExceptionConstants.DOES_NOT_EXISTS_USER + userId);
                throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_USER + userId);
            }

        } else {
            log.error(LogConstants.LIST_BANK_TRANSFER_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_LIST_BANK_TRANSFER);
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_LIST_BANK_TRANSFER);
        }

        return bankTransferDTOList;
    }
}
