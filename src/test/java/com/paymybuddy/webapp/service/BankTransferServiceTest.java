package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.BankTransferTypes;
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
import com.paymybuddy.webapp.testconstants.BankAccountTestConstants;
import com.paymybuddy.webapp.testconstants.BankTransferTestConstants;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import com.paymybuddy.webapp.util.DateUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
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
class BankTransferServiceTest {
    @MockBean
    private BankTransferRepository bankTransferRepositoryMock;

    @MockBean
    private BankAccountRepository bankAccountRepositoryMock;

    @MockBean
    private UserRepository userRepositoryMock;

    @Autowired
    private IBankTransferService bankTransferService;

    private static final DateUtil dateUtil = new DateUtil();

    private BankTransferDTO bankTransferDTOToCreate;

    private BankTransfer bankTransferInDb;
    private BankAccount bankAccountInDb;
    private User userInDb;

    @BeforeEach
    private void setUpPerTest() {
        bankTransferDTOToCreate = new BankTransferDTO();
        bankTransferDTOToCreate.setDate(dateUtil.getCurrentLocalDateTime());
        bankTransferDTOToCreate.setDescription(BankTransferTestConstants.NEW_BANK_TRANSFER_DESCRIPTION);
        bankTransferDTOToCreate.setAmount(BankTransferTestConstants.NEW_BANK_TRANSFER_AMOUNT);
        bankTransferDTOToCreate.setBankAccountId(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_ID);

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

        bankTransferInDb = new BankTransfer();
        bankTransferInDb.setBankTransferId(BankTransferTestConstants.NEW_BANK_TRANSFER_ID);
        bankTransferInDb.setDate(bankTransferDTOToCreate.getDate());
        //TOASK à voir si date n est pas plutôt la date d enregistrement plutôt que celle transmise dans DTO
        bankTransferInDb.setDescription(bankTransferDTOToCreate.getDescription());
        bankTransferInDb.setAmount(bankTransferDTOToCreate.getAmount());
        bankTransferInDb.setBankAccount(bankAccountInDb);
    }


    @Nested
    @DisplayName("transferFromBankAccount tests")
    class TransferFromBankAccountTest {

        @Test
        @DisplayName("GIVEN a new bank transfer from bank to add for an existing bank account " +
                "WHEN saving this new bank bank transfer " +
                "THEN the returned value is the added bank transfer and the user balance has been increased")
        void transferFromBankAccount_WithSuccess() throws PMBException {
            //GIVEN
            bankTransferInDb.setType(BankTransferTypes.CREDIT);
            when(bankAccountRepositoryMock.findById(bankTransferDTOToCreate.getBankAccountId()))
                    .thenReturn(Optional.ofNullable(bankAccountInDb));
            when(bankTransferRepositoryMock.save(any(BankTransfer.class))).thenReturn(bankTransferInDb);

            //WHEN
            Optional<BankTransferDTO> createdBankTransferDTO = bankTransferService.transferFromBankAccount(bankTransferDTOToCreate);

            //THEN
            assertTrue(createdBankTransferDTO.isPresent());
            assertNotNull(createdBankTransferDTO.get().getBankTransferId());
            assertEquals(bankTransferDTOToCreate.getBankAccountId(), createdBankTransferDTO.get().getBankAccountId());
            assertEquals(bankTransferDTOToCreate.getDate(), createdBankTransferDTO.get().getDate());
            assertEquals(bankTransferDTOToCreate.getDescription(), createdBankTransferDTO.get().getDescription());
            assertEquals(bankTransferDTOToCreate.getAmount(), createdBankTransferDTO.get().getAmount());

            verify(bankAccountRepositoryMock, Mockito.times(1))
                    .findById(bankTransferDTOToCreate.getBankAccountId());
            verify(userRepositoryMock, Mockito.times(1))
                    .save(any(User.class));
            verify(bankTransferRepositoryMock, Mockito.times(1))
                    .save(any(BankTransfer.class));
        }


        @Test
        @DisplayName("GIVEN a new bank transfer from bank to add with a non-existing bank account " +
                "WHEN saving this new bank transfer " +
                "THEN an PMB Exception is thrown")
        void transferFromBankAccount_WithNoExistingBankAccountInRepository() {
            //GIVEN
            when(bankAccountRepositoryMock.findById(bankTransferDTOToCreate.getBankAccountId()))
                    .thenReturn(Optional.empty());

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> bankTransferService.transferFromBankAccount(bankTransferDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.DOES_NOT_EXISTS_BANK_ACCOUNT);

            verify(bankAccountRepositoryMock, Mockito.times(1))
                    .findById(bankTransferDTOToCreate.getBankAccountId());
            verify(userRepositoryMock, Mockito.times(0))
                    .save(any(User.class));
            verify(bankTransferRepositoryMock, Mockito.times(0))
                    .save(any(BankTransfer.class));
        }


        @Test
        @DisplayName("GIVEN a new bank transfer from bank to add with missing informations" +
                "WHEN saving this new bank transfer " +
                "THEN an PMB Exception is thrown")
        void transferFromBankAccount_WithMissingInformations() {
            //GIVEN
            bankTransferDTOToCreate.setDescription(null);

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> bankTransferService.transferFromBankAccount(bankTransferDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_TRANSFER);

            verify(bankAccountRepositoryMock, Mockito.times(0))
                    .findById(bankTransferDTOToCreate.getBankAccountId());
            verify(userRepositoryMock, Mockito.times(0))
                    .save(any(User.class));
            verify(bankTransferRepositoryMock, Mockito.times(0))
                    .save(any(BankTransfer.class));
        }
    }


    @Nested
    @DisplayName("transferToBankAccount tests")
    class TransferToBankAccountTest {

        @Test
        @DisplayName("GIVEN a new bank transfer to bank to add for an existing bank account " +
                "WHEN saving this new bank transfer " +
                "THEN the returned value is the added bank transfer and the user balance has been decreased")
        void transferToBankAccount_WithSuccess() throws PMBException {
            //GIVEN
            bankTransferInDb.setType(BankTransferTypes.DEBIT);
            when(bankAccountRepositoryMock.findById(bankTransferDTOToCreate.getBankAccountId()))
                    .thenReturn(Optional.ofNullable(bankAccountInDb));
            when(bankTransferRepositoryMock.save(any(BankTransfer.class))).thenReturn(bankTransferInDb);

            //WHEN
            Optional<BankTransferDTO> createdBankTransferDTO = bankTransferService.transferToBankAccount(bankTransferDTOToCreate);

            //THEN
            assertTrue(createdBankTransferDTO.isPresent());
            assertNotNull(createdBankTransferDTO.get().getBankTransferId());
            assertEquals(bankTransferDTOToCreate.getBankAccountId(), createdBankTransferDTO.get().getBankAccountId());
            assertEquals(bankTransferDTOToCreate.getDate(), createdBankTransferDTO.get().getDate());
            assertEquals(bankTransferDTOToCreate.getDescription(), createdBankTransferDTO.get().getDescription());
            assertEquals(bankTransferDTOToCreate.getAmount(), createdBankTransferDTO.get().getAmount());

            verify(bankAccountRepositoryMock, Mockito.times(1))
                    .findById(bankTransferDTOToCreate.getBankAccountId());
            verify(userRepositoryMock, Mockito.times(1))
                    .save(any(User.class));
            verify(bankTransferRepositoryMock, Mockito.times(1))
                    .save(any(BankTransfer.class));
        }


        @Test
        @DisplayName("GIVEN a new bank transfer to bank to add for an existing bank account " +
                "but with an insufficient user's balance " +
                "WHEN saving this new bank transfer " +
                "THEN an PMB Exception is thrown")
        void transferToBankAccount_WithInsufficientUserBalance() {
            //GIVEN
            userInDb.setBalance(bankTransferDTOToCreate.getAmount().subtract(BigDecimal.valueOf(100)));
            when(bankAccountRepositoryMock.findById(bankTransferDTOToCreate.getBankAccountId()))
                    .thenReturn(Optional.ofNullable(bankAccountInDb));
            when(bankTransferRepositoryMock.save(any(BankTransfer.class))).thenReturn(bankTransferInDb);

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> bankTransferService.transferToBankAccount(bankTransferDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.INSUFFICIENT_BALANCE);

            verify(bankAccountRepositoryMock, Mockito.times(1))
                    .findById(bankTransferDTOToCreate.getBankAccountId());
            verify(userRepositoryMock, Mockito.times(0))
                    .save(any(User.class));
            verify(bankTransferRepositoryMock, Mockito.times(0))
                    .save(any(BankTransfer.class));
        }


        @Test
        @DisplayName("GIVEN a new bank transfer to bank to add with a non-existing bank account " +
                "WHEN saving this new bank transfer " +
                "THEN an PMB Exception is thrown")
        void transferToBankAccount_WithNoExistingBankAccountInRepository() {
            //GIVEN
            when(bankAccountRepositoryMock.findById(bankTransferDTOToCreate.getBankAccountId()))
                    .thenReturn(Optional.empty());

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> bankTransferService.transferToBankAccount(bankTransferDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.DOES_NOT_EXISTS_BANK_ACCOUNT);

            verify(bankAccountRepositoryMock, Mockito.times(1))
                    .findById(bankTransferDTOToCreate.getBankAccountId());
            verify(userRepositoryMock, Mockito.times(0))
                    .save(any(User.class));
            verify(bankTransferRepositoryMock, Mockito.times(0))
                    .save(any(BankTransfer.class));
        }


        @Test
        @DisplayName("GIVEN a new bank transfer to bank to add with missing informations" +
                "WHEN saving this new bank transfer " +
                "THEN an PMB Exception is thrown")
        void transferToBankAccount_WithMissingInformations() {
            //GIVEN
            bankTransferDTOToCreate.setDescription(null);

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> bankTransferService.transferToBankAccount(bankTransferDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_TRANSFER);

            verify(bankAccountRepositoryMock, Mockito.times(0))
                    .findById(bankTransferDTOToCreate.getBankAccountId());
            verify(userRepositoryMock, Mockito.times(0))
                    .save(any(User.class));
            verify(bankTransferRepositoryMock, Mockito.times(0))
                    .save(any(BankTransfer.class));
        }
    }


    @Nested
    @DisplayName("getAllBankTransfersForUser tests")
    class GetAllBankTransfersForUserTest {

        private BankTransfer bankTransferInDb;
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

            BankAccount bankAccountInDb = new BankAccount();
            bankAccountInDb.setBankAccountId(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_ID);
            bankAccountInDb.setIban(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_IBAN);
            bankAccountInDb.setName(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_NAME);
            bankAccountInDb.setUser(userInDb);

            bankTransferInDb = new BankTransfer();
            bankTransferInDb.setDate(dateUtil.getCurrentLocalDateTime());
            bankTransferInDb.setDescription(BankTransferTestConstants.EXISTING_BANK_TRANSFER_DESCRIPTION);
            bankTransferInDb.setAmount(BankTransferTestConstants.EXISTING_BANK_TRANSFER_AMOUNT);
            bankTransferInDb.setType(BankTransferTypes.CREDIT);
            bankTransferInDb.setBankAccount(bankAccountInDb);
        }


        @Test
        @DisplayName("GIVEN bank transfers in DB for an existing user " +
                "WHEN getting all the bank transfers for this user " +
                "THEN the returned value is the list of bank transfers")
        void getAllBankTransfersForUser_WithDataInDB() throws PMBException {
            //GIVEN
            List<BankTransfer> bankTransferList = new ArrayList<>();
            bankTransferList.add(bankTransferInDb);

            when(userRepositoryMock.findById(userInDb.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(bankTransferRepositoryMock.findAllByBankAccount_User_UserId(userInDb.getUserId()))
                    .thenReturn(bankTransferList);

            //THEN
            List<BankTransferDTO> bankTransferDTOList = bankTransferService.getAllBankTransfersForUser(userInDb.getUserId());
            assertEquals(1, bankTransferDTOList.size());
            assertEquals(bankTransferInDb.getBankTransferId(), bankTransferDTOList.get(0).getBankTransferId());

            verify(userRepositoryMock, Mockito.times(1)).findById(userInDb.getUserId());
            verify(bankTransferRepositoryMock, Mockito.times(1))
                    .findAllByBankAccount_User_UserId(userInDb.getUserId());
        }


        @Test
        @DisplayName("GIVEN no bank transfers in DB for an existing user " +
                "WHEN getting all the bank transfers for this user " +
                "THEN the returned value is an empty list of bank transfers")
        void getAllBankTransfersForUser_WithNoDataInDB() throws PMBException {
            //GIVEN
            List<BankTransfer> bankTransferList = new ArrayList<>();

            when(userRepositoryMock.findById(userInDb.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(bankTransferRepositoryMock.findAllByBankAccount_User_UserId(userInDb.getUserId()))
                    .thenReturn(bankTransferList);

            //THEN
            List<BankTransferDTO> bankTransferDTOList = bankTransferService.getAllBankTransfersForUser(userInDb.getUserId());
            assertThat(bankTransferDTOList).isEmpty();

            verify(userRepositoryMock, Mockito.times(1)).findById(userInDb.getUserId());
            verify(bankTransferRepositoryMock, Mockito.times(1))
                    .findAllByBankAccount_User_UserId(userInDb.getUserId());
        }


        @Test
        @DisplayName("GIVEN an unknown user " +
                "WHEN getting all the bank transfers for this user " +
                "THEN an PMB Exception is thrown")
        void getAllBankTransfersForUser_WithUnknownUser() {
            //GIVEN
            when(userRepositoryMock.findById(UserTestConstants.UNKNOWN_USER_ID))
                    .thenReturn(Optional.empty());

            //THEN
            Exception exception =
                    assertThrows(PMBException.class,
                            () -> bankTransferService.getAllBankTransfersForUser(UserTestConstants.UNKNOWN_USER_ID));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.DOES_NOT_EXISTS_USER);

            verify(userRepositoryMock, Mockito.times(1))
                    .findById(UserTestConstants.UNKNOWN_USER_ID);
            verify(bankTransferRepositoryMock, Mockito.times(0))
                    .findAllByBankAccount_User_UserId(UserTestConstants.UNKNOWN_USER_ID);
        }


        @Test
        @DisplayName("GIVEN an null userId " +
                "WHEN getting all the bank transfers for this user " +
                "THEN an PMB Exception is thrown")
        void getAllBankTransfersForUser_WithNullUserId() {
            //THEN
            Exception exception =
                    assertThrows(PMBException.class,
                            () -> bankTransferService.getAllBankTransfersForUser(null));
            assertThat(exception.getMessage())
                    .contains(PMBExceptionConstants.MISSING_INFORMATION_LIST_BANK_TRANSFER);

            verify(userRepositoryMock, Mockito.times(0))
                    .findById(anyLong());
            verify(bankTransferRepositoryMock, Mockito.times(0))
                    .findAllByBankAccount_User_UserId(anyLong());
        }
    }
}
