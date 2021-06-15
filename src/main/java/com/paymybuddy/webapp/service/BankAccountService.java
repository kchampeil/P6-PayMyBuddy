package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.BankAccount;
import com.paymybuddy.webapp.model.DTO.BankAccountDTO;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.BankAccountRepository;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.contract.IBankAccountService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class BankAccountService implements IBankAccountService {

    private final BankAccountRepository bankAccountRepository;

    private final UserRepository userRepository;

    @Autowired
    BankAccountService(BankAccountRepository bankAccountRepository, UserRepository userRepository) {
        this.bankAccountRepository = bankAccountRepository;
        this.userRepository = userRepository;
    }


    /**
     * création d un compte bancaire associé à un utilisateur en base
     *
     * @param bankAccountDTOToCreate compte bancaire à créer
     * @return objet BankAccountDTO contenant le compte bancaire créé
     * @throws PMBException si le compte bancaire existe déjà,
     *                      que l utilisateur n existe pas
     *                      que l IBAN est invalide
     *                      ou que des données sont manquantes
     */
    @Override
    public Optional<BankAccountDTO> createBankAccount(BankAccountDTO bankAccountDTOToCreate) throws PMBException {

        Optional<BankAccountDTO> createdBankAccountDTO = Optional.empty();

        if (checksBeforeCreatingBankAccount(bankAccountDTOToCreate)) {
            //mappe le DTO dans le DAO,
            // puis le nouveau compte bancaire est sauvegardé en base avant mappage inverse du DAO dans le DTO
            ModelMapper modelMapper = new ModelMapper();
            BankAccount createdBankAccount;

            try {
                createdBankAccount =
                        bankAccountRepository.save(modelMapper.map(bankAccountDTOToCreate, BankAccount.class));

            } catch (Exception exception) {
                log.error(LogConstants.CREATE_BANK_ACCOUNT_ERROR + bankAccountDTOToCreate.getName()
                        + " // " + bankAccountDTOToCreate.getIban());
                throw exception;
            }

            createdBankAccountDTO =
                    Optional.ofNullable(modelMapper.map((createdBankAccount), BankAccountDTO.class));
            log.info(LogConstants.CREATE_BANK_ACCOUNT_OK + bankAccountDTOToCreate.getBankAccountId());
        }

        return createdBankAccountDTO;
    }


    /**
     * récupération de la liste de tous les comptes bancaires d un utilisateur donné
     *
     * @param userId id de l utilisateur dont on souhaite à récupérer la liste des comptes bancaires
     * @return la liste des comptes bancaires (DTO)
     */
    @Override
    public List<BankAccountDTO> getAllBankAccountsForUser(Long userId) throws PMBException {
        List<BankAccountDTO> bankAccountDTOList = new ArrayList<>();

        if (checksBeforeGettingBankAccounts(userId)) {
            List<BankAccount> bankAccountList = bankAccountRepository.findAllByUser_UserId(userId);
            ModelMapper modelMapper = new ModelMapper();
            bankAccountList.forEach(bankAccount ->
                    bankAccountDTOList.add(modelMapper.map(bankAccount, BankAccountDTO.class)));
            log.info(LogConstants.LIST_BANK_ACCOUNT_OK + bankAccountDTOList.size());
        }

        return bankAccountDTOList;
    }


    /**
     * vérifie que les informations transmises sont correctes en amont de la création du compte bancaire
     *
     * @param bankAccountDTOToCreate contient les informations sur le compte bancaire à créer
     * @return true si tout est correct
     * @throws PMBException si des données sont manquantes
     *                      ou que l IBAN est invalide
     *                      ou que l utilisateur n existe pas
     *                      ou que le compte bancaire existe déjà
     */
    private boolean checksBeforeCreatingBankAccount(BankAccountDTO bankAccountDTOToCreate) throws PMBException {
        //vérifie qu il ne manque pas d informations
        if (!bankAccountDTOToCreate.isValid()) {
            log.error(LogConstants.CREATE_BANK_ACCOUNT_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_ACCOUNT + "for: " + bankAccountDTOToCreate.getName()
                    + " // " + bankAccountDTOToCreate.getIban());
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_ACCOUNT);
        }

        //vérifie que l'IBAN est valide
        if (!bankAccountDTOToCreate.hasValidIban()) {
            log.error(LogConstants.CREATE_BANK_ACCOUNT_ERROR
                    + PMBExceptionConstants.INVALID_IBAN + bankAccountDTOToCreate.getIban());
            throw new PMBException(PMBExceptionConstants.INVALID_IBAN + bankAccountDTOToCreate.getIban());
        }

        //vérifie que l'utilisateur associé existe bien
        if (!userRepository.findById(bankAccountDTOToCreate.getUserId()).isPresent()) {
            log.error(LogConstants.CREATE_BANK_ACCOUNT_ERROR
                    + PMBExceptionConstants.DOES_NOT_EXISTS_USER + bankAccountDTOToCreate.getUserId());
            throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_USER + bankAccountDTOToCreate.getUserId());
        }

        //vérifie que le compte bancaire n'existe pas déjà pour l'utilisateur
        if (bankAccountRepository
                .findByIbanAndUser_UserId(bankAccountDTOToCreate.getIban(), bankAccountDTOToCreate.getUserId()).isPresent()) {
            log.error(LogConstants.CREATE_BANK_ACCOUNT_ERROR
                    + PMBExceptionConstants.ALREADY_EXIST_BANK_ACCOUNT
                    + bankAccountDTOToCreate.getIban() + " // " + bankAccountDTOToCreate.getUserId());
            throw new PMBException(PMBExceptionConstants.ALREADY_EXIST_BANK_ACCOUNT
                    + bankAccountDTOToCreate.getIban() + " // " + bankAccountDTOToCreate.getUserId());
        }
        return true;
    }


    /**
     * vérifie que les informations transmises sont correctes en amont de la récupération de la liste des comptes bancaires
     *
     * @param userId identifiant de l'utilisateur pour lequel on souhaite récupérer la liste des comptes bancaires
     * @return true si tout est correct
     * @throws PMBException si l'identifiant transmis est nul
     *                      ou que l'utilisateur n'existe pas
     */
    private boolean checksBeforeGettingBankAccounts(Long userId) throws PMBException {
        //vérifie qu il ne manque pas d informations
        if (userId == null) {
            log.error(LogConstants.LIST_BANK_ACCOUNT_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_LIST_BANK_ACCOUNT);
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_LIST_BANK_ACCOUNT);
        }

        //vérifie que l'utilisateur existe bien
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            log.error(LogConstants.LIST_BANK_ACCOUNT_ERROR
                    + PMBExceptionConstants.DOES_NOT_EXISTS_USER + userId);
            throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_USER + userId);
        }

        return true;
    }
}
