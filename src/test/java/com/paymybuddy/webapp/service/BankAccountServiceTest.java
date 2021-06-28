package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.BankAccount;
import com.paymybuddy.webapp.model.DTO.BankAccountDTO;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.BankAccountRepository;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.contract.IBankAccountService;
import com.paymybuddy.webapp.testconstants.BankAccountTestConstants;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class BankAccountServiceTest {

    @MockBean
    private BankAccountRepository bankAccountRepositoryMock;

    @MockBean
    private UserRepository userRepositoryMock;

    @Autowired
    private IBankAccountService bankAccountService;

    @Nested
    @DisplayName("createBankAccount tests")
    class CreateBankAccountTest {
        private BankAccountDTO bankAccountDTOToCreate;

        private BankAccount bankAccountInDb;
        private User userInDb;

        @BeforeEach
        private void setUpPerTest() {
            bankAccountDTOToCreate = new BankAccountDTO();
            bankAccountDTOToCreate.setIban(BankAccountTestConstants.NEW_BANK_ACCOUNT_IBAN);
            bankAccountDTOToCreate.setName(BankAccountTestConstants.NEW_BANK_ACCOUNT_NAME);
            bankAccountDTOToCreate.setUserId(UserTestConstants.EXISTING_USER_ID);

            userInDb = new User();
            userInDb.setUserId(UserTestConstants.EXISTING_USER_ID);
            userInDb.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
            userInDb.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
            userInDb.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
            userInDb.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
            userInDb.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);

            bankAccountInDb = new BankAccount();
            bankAccountInDb.setBankAccountId(BankAccountTestConstants.NEW_BANK_ACCOUNT_ID);
            bankAccountInDb.setIban(bankAccountDTOToCreate.getIban());
            bankAccountInDb.setName(bankAccountDTOToCreate.getName());
            bankAccountInDb.setUser(userInDb);
        }


        @Test
        @DisplayName("GIVEN a new bank account to add " +
                "WHEN saving this new bank account " +
                "THEN the returned value is the added bank account")
        void createBankAccount_WithSuccess() throws PMBException {
            //GIVEN
            when(bankAccountRepositoryMock
                    .findByIbanAndUser_UserId(bankAccountDTOToCreate.getIban(), bankAccountDTOToCreate.getUserId()))
                    .thenReturn(Optional.empty());
            when(userRepositoryMock.findById(bankAccountDTOToCreate.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(bankAccountRepositoryMock.save(any(BankAccount.class))).thenReturn(bankAccountInDb);

            //WHEN
            Optional<BankAccountDTO> createdBankAccountDTO = bankAccountService.createBankAccount(bankAccountDTOToCreate);

            //THEN
            assertTrue(createdBankAccountDTO.isPresent());
            assertNotNull(createdBankAccountDTO.get().getBankAccountId());
            assertEquals(bankAccountDTOToCreate.getUserId(), createdBankAccountDTO.get().getUserId());
            assertEquals(bankAccountDTOToCreate.getName(), createdBankAccountDTO.get().getName());
            assertEquals(bankAccountDTOToCreate.getIban(), createdBankAccountDTO.get().getIban());

            verify(userRepositoryMock, Mockito.times(1))
                    .findById(bankAccountDTOToCreate.getUserId());
            verify(bankAccountRepositoryMock, Mockito.times(1))
                    .findByIbanAndUser_UserId(bankAccountDTOToCreate.getIban(), bankAccountDTOToCreate.getUserId());
            verify(bankAccountRepositoryMock, Mockito.times(1))
                    .save(any(BankAccount.class));
        }


        @Test
        @DisplayName("GIVEN a new bank account to add with an already existing bank account for this user " +
                "WHEN saving this new bank account " +
                "THEN an PMB Exception is thrown")
        void createBankAccount_WithExistingBankAccountInRepository() {
            //GIVEN
            when(userRepositoryMock.findById(bankAccountDTOToCreate.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(bankAccountRepositoryMock
                    .findByIbanAndUser_UserId(bankAccountDTOToCreate.getIban(), bankAccountDTOToCreate.getUserId()))
                    .thenReturn(Optional.of(bankAccountInDb));

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> bankAccountService.createBankAccount(bankAccountDTOToCreate));
            assertEquals(PMBExceptionConstants.ALREADY_EXIST_BANK_ACCOUNT, exception.getMessage());

            verify(userRepositoryMock, Mockito.times(1))
                    .findById(bankAccountDTOToCreate.getUserId());
            verify(bankAccountRepositoryMock, Mockito.times(1))
                    .findByIbanAndUser_UserId(bankAccountDTOToCreate.getIban(), bankAccountDTOToCreate.getUserId());
            verify(bankAccountRepositoryMock, Mockito.times(0))
                    .save(any(BankAccount.class));
        }


        @Test
        @DisplayName("GIVEN a new bank account to add with missing informations " +
                "WHEN saving this new bank account " +
                "THEN an PMB Exception is thrown")
        void createBankAccount_WithMissingInformations() {
            //GIVEN
            bankAccountDTOToCreate.setName(null);

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> bankAccountService.createBankAccount(bankAccountDTOToCreate));
            assertEquals(PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_ACCOUNT, exception.getMessage());

            verify(userRepositoryMock, Mockito.times(0))
                    .findById(bankAccountDTOToCreate.getUserId());
            verify(bankAccountRepositoryMock, Mockito.times(0))
                    .findByIbanAndUser_UserId(bankAccountDTOToCreate.getIban(), bankAccountDTOToCreate.getUserId());
            verify(bankAccountRepositoryMock, Mockito.times(0))
                    .save(any(BankAccount.class));
        }


        @Test
        @DisplayName("GIVEN a new bank account to add with invalid IBAN " +
                "WHEN saving this new bank account " +
                "THEN an PMB Exception is thrown")
        void createBankAccount_WithInvalidIBAN() {
            //GIVEN
            bankAccountDTOToCreate.setIban(BankAccountTestConstants.NEW_BANK_ACCOUNT_INVALID_IBAN);

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> bankAccountService.createBankAccount(bankAccountDTOToCreate));
            assertEquals(PMBExceptionConstants.INVALID_IBAN, exception.getMessage());

            verify(userRepositoryMock, Mockito.times(0))
                    .findById(bankAccountDTOToCreate.getUserId());
            verify(bankAccountRepositoryMock, Mockito.times(0))
                    .findByIbanAndUser_UserId(bankAccountDTOToCreate.getIban(), bankAccountDTOToCreate.getUserId());
            verify(bankAccountRepositoryMock, Mockito.times(0))
                    .save(any(BankAccount.class));
        }
    }

    @Nested
    @DisplayName("getAllBankAccountsForUser tests")
    class GetAllBankAccountsForUserTest {

        private BankAccount bankAccountInDb;
        private User userInDb;

        @BeforeEach
        private void setUpPerTest() {
            userInDb = new User();
            userInDb.setUserId(UserTestConstants.EXISTING_USER_ID);
            userInDb.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
            userInDb.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
            userInDb.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
            userInDb.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
            userInDb.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);

            bankAccountInDb = new BankAccount();
            bankAccountInDb.setBankAccountId(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_ID);
            bankAccountInDb.setIban(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_IBAN);
            bankAccountInDb.setName(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_NAME);
            bankAccountInDb.setUser(userInDb);
        }


        @Test
        @DisplayName("GIVEN bank accounts in DB for an existing user " +
                "WHEN getting all the bank accounts for this user " +
                "THEN the returned value is the list of bank accounts")
        void getAllBankAccountsForUser_WithDataInDB() throws PMBException {
            //GIVEN
            List<BankAccount> bankAccountList = new ArrayList<>();
            bankAccountList.add(bankAccountInDb);

            when(userRepositoryMock.findById(userInDb.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(bankAccountRepositoryMock.findAllByUser_UserId(userInDb.getUserId()))
                    .thenReturn(bankAccountList);

            //THEN
            List<BankAccountDTO> bankAccountDTOList =
                    bankAccountService.getAllBankAccountsForUser(userInDb.getUserId());
            assertEquals(1, bankAccountDTOList.size());
            assertEquals(bankAccountInDb.getBankAccountId(), bankAccountDTOList.get(0).getBankAccountId());

            verify(userRepositoryMock, Mockito.times(1)).findById(userInDb.getUserId());
            verify(bankAccountRepositoryMock, Mockito.times(1))
                    .findAllByUser_UserId(userInDb.getUserId());
        }


        @Test
        @DisplayName("GIVEN no bank accounts in DB for an existing user " +
                "WHEN getting all the bank accounts for this user " +
                "THEN the returned value is an empty list of bank accounts")
        void getAllBankAccountsForUser_WithNoDataInDB() throws PMBException {
            //GIVEN
            List<BankAccount> bankAccountList = new ArrayList<>();

            when(userRepositoryMock.findById(userInDb.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(bankAccountRepositoryMock.findAllByUser_UserId(userInDb.getUserId()))
                    .thenReturn(bankAccountList);

            //THEN
            List<BankAccountDTO> bankAccountDTOList =
                    bankAccountService.getAllBankAccountsForUser(userInDb.getUserId());
            assertThat(bankAccountDTOList).isEmpty();

            verify(userRepositoryMock, Mockito.times(1)).findById(userInDb.getUserId());
            verify(bankAccountRepositoryMock, Mockito.times(1))
                    .findAllByUser_UserId(userInDb.getUserId());
        }


        @Test
        @DisplayName("GIVEN an unknown user " +
                "WHEN getting all the bank accounts for this user " +
                "THEN an PMB Exception is thrown")
        void getAllBankAccountsForUser_WithUnknownUser() {
            //GIVEN
            when(userRepositoryMock.findById(UserTestConstants.UNKNOWN_USER_ID))
                    .thenReturn(Optional.empty());

            //THEN
            Exception exception = assertThrows(PMBException.class,
                    () -> bankAccountService.getAllBankAccountsForUser(UserTestConstants.UNKNOWN_USER_ID));
            assertEquals(PMBExceptionConstants.DOES_NOT_EXISTS_USER, exception.getMessage());

            verify(userRepositoryMock, Mockito.times(1))
                    .findById(UserTestConstants.UNKNOWN_USER_ID);
            verify(bankAccountRepositoryMock, Mockito.times(0))
                    .findAllByUser_UserId(UserTestConstants.UNKNOWN_USER_ID);
        }


        @Test
        @DisplayName("GIVEN an null userId " +
                "WHEN getting all the bank accounts for this user " +
                "THEN an PMB Exception is thrown")
        void getAllBankAccountsForUser_WithNullUserId() {
            //THEN
            Exception exception = assertThrows(PMBException.class,
                    () -> bankAccountService.getAllBankAccountsForUser(null));
            assertEquals(PMBExceptionConstants.MISSING_INFORMATION_LIST_BANK_ACCOUNT, exception.getMessage());

            verify(userRepositoryMock, Mockito.times(0)).findById(anyLong());
            verify(bankAccountRepositoryMock, Mockito.times(0)).findAllByUser_UserId(anyLong());
        }
    }
}
