package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.BankAccount;
import com.paymybuddy.webapp.model.DTO.BankAccountDTO;
import com.paymybuddy.webapp.repository.BankAccountRepository;
import com.paymybuddy.webapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
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
        Optional<BankAccountDTO> createdBankAccountDTO;

        //vérifie qu il ne manque pas d informations
        if (bankAccountDTOToCreate.isValid()) {

            //vérifie que l IBAN est valide
            if (bankAccountDTOToCreate.ibanIsValid()) {

                //vérifie que l utilisateur associé existe bien
                if (userRepository.findById(bankAccountDTOToCreate.getUserId()).isPresent()) {

                    //vérifie que le compte bancaire n existe pas déjà pour l utilisateur
                    if (!bankAccountRepository.findByIbanAndUser_UserId(bankAccountDTOToCreate.getIban(), bankAccountDTOToCreate.getUserId()).isPresent()) {

                        //mappe le DTO dans le DAO,
                        // puis le nouveau compte bancaire est sauvegardé en base avant mappage inverse du DAO dans le DTO
                        ModelMapper modelMapper = new ModelMapper();

                        try {
                            BankAccount createdBankAccount =
                                    bankAccountRepository.save(modelMapper.map(bankAccountDTOToCreate, BankAccount.class));
                            createdBankAccountDTO =
                                    Optional.ofNullable(modelMapper.map((createdBankAccount), BankAccountDTO.class));
                            log.info(LogConstants.CREATE_BANK_ACCOUNT_OK + bankAccountDTOToCreate.getName()
                                    + " // " + bankAccountDTOToCreate.getIban());

                        } catch (Exception exception) {
                            log.error(LogConstants.CREATE_BANK_ACCOUNT_ERROR + bankAccountDTOToCreate.getName()
                                    + " // " + bankAccountDTOToCreate.getIban());
                            throw exception;
                        }
                    } else {
                        log.error(LogConstants.CREATE_BANK_ACCOUNT_ERROR
                                + PMBExceptionConstants.ALREADY_EXIST_BANK_ACCOUNT
                                + bankAccountDTOToCreate.getIban()+" // "+bankAccountDTOToCreate.getUserId());
                        throw new PMBException(PMBExceptionConstants.ALREADY_EXIST_BANK_ACCOUNT
                                + bankAccountDTOToCreate.getIban()+" // "+bankAccountDTOToCreate.getUserId());
                    }

                } else {
                    log.error(LogConstants.CREATE_BANK_ACCOUNT_ERROR
                            + PMBExceptionConstants.DOES_NOT_EXISTS_USER + bankAccountDTOToCreate.getUserId());
                    throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_USER + bankAccountDTOToCreate.getUserId());

                }

            } else {
                log.error(LogConstants.CREATE_BANK_ACCOUNT_ERROR
                        + PMBExceptionConstants.INVALID_IBAN + bankAccountDTOToCreate.getIban());
                throw new PMBException(PMBExceptionConstants.INVALID_IBAN + bankAccountDTOToCreate.getIban());
            }

        } else {
            log.error(LogConstants.CREATE_BANK_ACCOUNT_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_ACCOUNT + "for: " + bankAccountDTOToCreate.getName()
                    + " // " + bankAccountDTOToCreate.getIban());
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_ACCOUNT);
        }

        return createdBankAccountDTO;
    }
}
