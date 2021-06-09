package com.paymybuddy.webapp.integration;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.BankAccount;
import com.paymybuddy.webapp.model.DTO.BankAccountDTO;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.BankAccountRepository;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.IBankAccountService;
import com.paymybuddy.webapp.testconstants.BankAccountTestConstants;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest
public class BankAccountServiceIT {
    @Autowired
    IBankAccountService bankAccountService;

    @Autowired
    BankAccountRepository bankAccountRepository;

    @Autowired
    UserRepository userRepository;

    @Nested
    @DisplayName("createBankAccount IT")
    class CreateBankAccountIT {

        private User existingUser;
        private BankAccountDTO bankAccountDTOToCreate;


        @BeforeEach
        private void setUpPerTest() {
            //initialisation avec un user en base
            existingUser = new User();
            existingUser.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
            existingUser.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
            existingUser.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
            existingUser.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
            existingUser.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
            existingUser = userRepository.save(existingUser);

            //initialisation de l objet bankAccountDTOToCreate
            bankAccountDTOToCreate = new BankAccountDTO();
            bankAccountDTOToCreate.setIban(BankAccountTestConstants.NEW_BANK_ACCOUNT_IBAN);
            bankAccountDTOToCreate.setName(BankAccountTestConstants.NEW_BANK_ACCOUNT_NAME);
            bankAccountDTOToCreate.setUserId(existingUser.getUserId());
        }

        @AfterEach
        private void tearDownPerTest() {
            //nettoyage la DB en fin de test en supprimant le user créé à l initialisation
            //TOASK le premier user créé n est pas supprimé
            // comme si la base ne pouvait pas être vide une fois alimentée(même avec un deleteAll ça ne la supprime pas)
            // userRepository.deleteAll();
            userRepository.deleteById(existingUser.getUserId());
        }


        @Test
        @DisplayName("WHEN creating a new bank account with correct informations for an existing user " +
                "THEN the returned value is the added bank account, " +
                "AND the bank account is added in DB")
        public void createBankAccountIT_WithSuccess() throws Exception {

            Optional<BankAccountDTO> bankAccountDTOCreated = bankAccountService.createBankAccount(bankAccountDTOToCreate);
            Optional<BankAccount> bankAccountCreated = bankAccountRepository
                    .findByIbanAndUser_UserId(bankAccountDTOToCreate.getIban(), existingUser.getUserId());

            assertThat(bankAccountDTOCreated).isPresent();
            assertThat(bankAccountCreated).isPresent();
            assertEquals(bankAccountDTOToCreate.getName(), bankAccountCreated.get().getName());

            //nettoyage de la DB en fin de test en supprimant le compte bancaire créé par le test
            bankAccountRepository.deleteById(bankAccountCreated.get().getBankAccountId());
        }


        @Test
        @DisplayName("WHEN creating a new bank account with invalid IBAN " +
                "THEN an PMBException is thrown AND the bank account is not added in DB")
        public void createBankAccountIT_InvalidData() {

            bankAccountDTOToCreate.setIban(BankAccountTestConstants.NEW_BANK_ACCOUNT_INVALID_IBAN);

            Exception exception = assertThrows(PMBException.class, () -> bankAccountService.createBankAccount(bankAccountDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.INVALID_IBAN);

            Optional<BankAccount> bankAccountWithIbanAndUser = bankAccountRepository
                    .findByIbanAndUser_UserId(bankAccountDTOToCreate.getIban(),existingUser.getUserId());
            assertThat(bankAccountWithIbanAndUser).isNotPresent();
        }


        @Test
        @DisplayName("WHEN creating a new bank account with an existing IBAN for the user " +
                "THEN an PMBException is thrown AND the bank account is not added in DB")
        @Transactional
        public void createBankAccountIT_AlreadyExists() {
            //initialisation du test avec un compte bancaire en base
            BankAccount existingBankAccount = new BankAccount();
            existingBankAccount.setIban(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_IBAN);
            existingBankAccount.setName(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_NAME);
            existingBankAccount.setUser(existingUser);
            existingBankAccount = bankAccountRepository.save(existingBankAccount);

            //test
            bankAccountDTOToCreate.setIban(existingBankAccount.getIban());

            Exception exception = assertThrows(PMBException.class, () -> bankAccountService.createBankAccount(bankAccountDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.ALREADY_EXIST_BANK_ACCOUNT);

            Optional<BankAccount> bankAccountAlreadyExisting = bankAccountRepository
                    .findByIbanAndUser_UserId(bankAccountDTOToCreate.getIban(),existingUser.getUserId());
            assertThat(bankAccountAlreadyExisting).isNotEmpty();
            assertEquals(existingBankAccount, bankAccountAlreadyExisting.get());

            //nettoyage de la DB en fin de test en supprimant le compte bancaire créé à l initialisation du test
            //TOASK le premier CB créé n est pas supprimé comme si la base ne pouvait pas être vide une fois alimentée
            bankAccountRepository.deleteById(existingBankAccount.getBankAccountId());
        }


        @Test
        @DisplayName("WHEN getting the list of bank accounts for an existing user " +
                "THEN the list of bank accounts in DB is returned")
        @Transactional
        public void getAllBankAccountsForUser_WithData() throws PMBException {
            
            //initialisation du test avec un compte bancaire en base
            BankAccount existingBankAccount = new BankAccount();
            existingBankAccount.setIban(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_IBAN);
            existingBankAccount.setName(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_NAME);
            existingBankAccount.setUser(existingUser);
            existingBankAccount = bankAccountRepository.save(existingBankAccount);

            //test
            List<BankAccountDTO> bankAccountDTOList = bankAccountService.getAllBankAccountsForUser(existingUser.getUserId());

            assertThat(bankAccountDTOList).isNotEmpty();
            assertEquals(existingBankAccount.getBankAccountId(), bankAccountDTOList.get(0).getBankAccountId());

            //nettoyage de la DB en fin de test en supprimant le compte bancaire créé par le test
            bankAccountRepository.deleteById(existingBankAccount.getBankAccountId());
        }
    }
}
