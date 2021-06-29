package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.TransactionDTO;
import com.paymybuddy.webapp.model.Relationship;
import com.paymybuddy.webapp.model.Transaction;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.RelationshipRepository;
import com.paymybuddy.webapp.repository.TransactionRepository;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.contract.ITransactionService;
import com.paymybuddy.webapp.testconstants.RelationshipTestConstants;
import com.paymybuddy.webapp.testconstants.TransactionTestConstants;
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
class TransactionServiceTest {

    @MockBean
    private TransactionRepository transactionRepositoryMock;

    @MockBean
    private RelationshipRepository relationshipRepositoryMock;

    @MockBean
    private UserRepository userRepositoryMock;

    @Autowired
    private ITransactionService transactionService;

    private static final DateUtil dateUtil = new DateUtil();

    private TransactionDTO transactionDTOToCreate;

    private Transaction transactionInDb;
    private Relationship relationshipInDb;
    private User userInDb;


    @BeforeEach
    private void setUpPerTest() {
        transactionDTOToCreate = new TransactionDTO();
        transactionDTOToCreate.setDescription(TransactionTestConstants.NEW_TRANSACTION_DESCRIPTION);
        transactionDTOToCreate.setAmountFeeExcluded(TransactionTestConstants.NEW_TRANSACTION_AMOUNT_FEE_EXCLUDED);
        transactionDTOToCreate.setRelationshipId(RelationshipTestConstants.EXISTING_RELATIONSHIP_ID);

        userInDb = new User();
        userInDb.setUserId(UserTestConstants.EXISTING_USER_ID);
        userInDb.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
        userInDb.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
        userInDb.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
        userInDb.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
        userInDb.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);

        User friendInDb = new User();
        friendInDb.setUserId(UserTestConstants.EXISTING_USER_AS_FRIEND_ID);
        friendInDb.setEmail(UserTestConstants.EXISTING_USER_AS_FRIEND_EMAIL);
        friendInDb.setFirstname(UserTestConstants.EXISTING_USER_AS_FRIEND_FIRSTNAME);
        friendInDb.setLastname(UserTestConstants.EXISTING_USER_AS_FRIEND_LASTNAME);
        friendInDb.setPassword(UserTestConstants.EXISTING_USER_AS_FRIEND_PASSWORD);
        friendInDb.setBalance(UserTestConstants.EXISTING_USER_AS_FRIEND_BALANCE);

        relationshipInDb = new Relationship();
        relationshipInDb.setRelationshipId(RelationshipTestConstants.EXISTING_RELATIONSHIP_ID);
        relationshipInDb.setUser(userInDb);
        relationshipInDb.setFriend(friendInDb);

        transactionInDb = new Transaction();
        transactionInDb.setTransactionId(TransactionTestConstants.NEW_TRANSACTION_ID);
        transactionInDb.setDate(dateUtil.getCurrentLocalDateTime());
        transactionInDb.setDescription(transactionDTOToCreate.getDescription());
        transactionInDb.setAmountFeeExcluded(transactionDTOToCreate.getAmountFeeExcluded());
        transactionInDb.setFeeAmount(transactionDTOToCreate.getFeeAmount());
        transactionInDb.setFeeBilled(false);
        transactionInDb.setRelationship(relationshipInDb);
    }


    @Nested
    @DisplayName("transferToFriend tests")
    class TransferToFriendTest {

        @Test
        @DisplayName("GIVEN a new transaction to a friend to add for an existing relationship " +
                "WHEN saving this new transaction " +
                "THEN the returned value is the added transaction " +
                "AND the user balance has been decreased of the transaction amount + fees " +
                "AND the friend balance has been increased of the transaction amount")
        void transferToFriend_WithSuccess() throws PMBException {
            //GIVEN
            when(relationshipRepositoryMock.findById(transactionDTOToCreate.getRelationshipId()))
                    .thenReturn(Optional.ofNullable(relationshipInDb));
            when(transactionRepositoryMock.save(any(Transaction.class))).thenReturn(transactionInDb);

            //WHEN
            Optional<TransactionDTO> createdTransactionDTO = transactionService.transferToFriend(transactionDTOToCreate);

            //THEN
            assertTrue(createdTransactionDTO.isPresent());
            assertNotNull(createdTransactionDTO.get().getTransactionId());
            assertEquals(transactionDTOToCreate.getRelationshipId(), createdTransactionDTO.get().getRelationshipId());
            assertNotNull(createdTransactionDTO.get().getDate());
            assertEquals(transactionDTOToCreate.getDescription(), createdTransactionDTO.get().getDescription());
            assertEquals(transactionDTOToCreate.getAmountFeeExcluded(), createdTransactionDTO.get().getAmountFeeExcluded());

            verify(relationshipRepositoryMock, Mockito.times(1))
                    .findById(transactionDTOToCreate.getRelationshipId());
            verify(userRepositoryMock, Mockito.times(2))
                    .save(any(User.class));
            verify(transactionRepositoryMock, Mockito.times(1))
                    .save(any(Transaction.class));
        }


        @Test
        @DisplayName("GIVEN a new transaction to a friend to add for an existing relationship " +
                "but with an insufficient user's balance for transaction + fee amount " +
                "WHEN saving this new transaction " +
                "THEN an PMB Exception is thrown")
        void transferToFriend_WithInsufficientUserBalance() {
            //GIVEN
            BigDecimal insufficientBalance = transactionDTOToCreate.getAmountFeeExcluded()
                    .add(transactionDTOToCreate.getFeeAmount())
                    .subtract(BigDecimal.valueOf(1));
            userInDb.setBalance(insufficientBalance);
            when(relationshipRepositoryMock.findById(transactionDTOToCreate.getRelationshipId()))
                    .thenReturn(Optional.ofNullable(relationshipInDb));
            when(transactionRepositoryMock.save(any(Transaction.class))).thenReturn(transactionInDb);

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> transactionService.transferToFriend(transactionDTOToCreate));
            assertEquals(PMBExceptionConstants.INSUFFICIENT_BALANCE, exception.getMessage());

            verify(relationshipRepositoryMock, Mockito.times(1))
                    .findById(transactionDTOToCreate.getRelationshipId());
            verify(userRepositoryMock, Mockito.times(0))
                    .save(any(User.class));
            verify(transactionRepositoryMock, Mockito.times(0))
                    .save(any(Transaction.class));
        }


        @Test
        @DisplayName("GIVEN a new transaction to a friend to add with a non-existing relationship " +
                "WHEN saving this new transaction " +
                "THEN an PMB Exception is thrown")
        void transferToFriend_WithNoExistingRelationshipInRepository() {
            //GIVEN
            when(relationshipRepositoryMock.findById(transactionDTOToCreate.getRelationshipId()))
                    .thenReturn(Optional.empty());

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> transactionService.transferToFriend(transactionDTOToCreate));
            assertEquals(PMBExceptionConstants.DOES_NOT_EXISTS_RELATIONSHIP, exception.getMessage());

            verify(relationshipRepositoryMock, Mockito.times(1))
                    .findById(transactionDTOToCreate.getRelationshipId());
            verify(userRepositoryMock, Mockito.times(0))
                    .save(any(User.class));
            verify(transactionRepositoryMock, Mockito.times(0))
                    .save(any(Transaction.class));
        }


        @Test
        @DisplayName("GIVEN a new transaction to a friend to add with missing informations" +
                "WHEN saving this new transaction " +
                "THEN an PMB Exception is thrown")
        void transferToFriend_WithMissingInformations() {
            //GIVEN
            transactionDTOToCreate.setDescription(null);

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> transactionService.transferToFriend(transactionDTOToCreate));
            assertEquals(PMBExceptionConstants.MISSING_INFORMATION_NEW_TRANSACTION, exception.getMessage());

            verify(relationshipRepositoryMock, Mockito.times(1))
                    .findById(transactionDTOToCreate.getRelationshipId());
            verify(userRepositoryMock, Mockito.times(0))
                    .save(any(User.class));
            verify(transactionRepositoryMock, Mockito.times(0))
                    .save(any(Transaction.class));
        }
    }


    @Nested
    @DisplayName("getAllTransactionsForUser tests")
    class GetAllTransactionsForUserTest {

        private Transaction transactionInDb;
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

            User friendInDb = new User();
            friendInDb.setUserId(UserTestConstants.EXISTING_USER_AS_FRIEND_ID);
            friendInDb.setEmail(UserTestConstants.EXISTING_USER_AS_FRIEND_EMAIL);
            friendInDb.setFirstname(UserTestConstants.EXISTING_USER_AS_FRIEND_FIRSTNAME);
            friendInDb.setLastname(UserTestConstants.EXISTING_USER_AS_FRIEND_LASTNAME);
            friendInDb.setPassword(UserTestConstants.EXISTING_USER_AS_FRIEND_PASSWORD);
            friendInDb.setBalance(UserTestConstants.EXISTING_USER_AS_FRIEND_BALANCE);

            Relationship relationshipInDb = new Relationship();
            relationshipInDb.setRelationshipId(RelationshipTestConstants.EXISTING_RELATIONSHIP_ID);
            relationshipInDb.setUser(userInDb);
            relationshipInDb.setFriend(friendInDb);

            transactionInDb = new Transaction();
            transactionInDb.setDate(dateUtil.getCurrentLocalDateTime());
            transactionInDb.setDescription(TransactionTestConstants.EXISTING_TRANSACTION_DESCRIPTION);
            transactionInDb.setAmountFeeExcluded(TransactionTestConstants.EXISTING_TRANSACTION_AMOUNT_FEE_EXCLUDED);
            transactionInDb.setFeeAmount(TransactionTestConstants.EXISTING_TRANSACTION_FEE_AMOUNT);
            transactionInDb.setFeeBilled(false);
            transactionInDb.setRelationship(relationshipInDb);
        }


        @Test
        @DisplayName("GIVEN transactions in DB for an existing user " +
                "WHEN getting all the transactions for this user " +
                "THEN the returned value is the list of transactions")
        void getAllTransactionsForUser_WithDataInDB() throws PMBException {
            //GIVEN
            List<Transaction> transactionList = new ArrayList<>();
            transactionList.add(transactionInDb);

            when(userRepositoryMock.findById(userInDb.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(transactionRepositoryMock.findAllByRelationship_User_UserIdOrderByDateDesc(userInDb.getUserId()))
                    .thenReturn(transactionList);

            //THEN
            List<TransactionDTO> transactionDTOList = transactionService.getAllTransactionsForUser(userInDb.getUserId());
            assertEquals(1, transactionDTOList.size());
            assertEquals(transactionInDb.getTransactionId(), transactionDTOList.get(0).getTransactionId());

            verify(userRepositoryMock, Mockito.times(1)).findById(userInDb.getUserId());
            verify(transactionRepositoryMock, Mockito.times(1))
                    .findAllByRelationship_User_UserIdOrderByDateDesc(userInDb.getUserId());
        }


        @Test
        @DisplayName("GIVEN no transactions in DB for an existing user " +
                "WHEN getting all the transactions for this user " +
                "THEN the returned value is an empty list of transactions")
        void getAllTransactionsForUser_WithNoDataInDB() throws PMBException {
            //GIVEN
            List<Transaction> transactionList = new ArrayList<>();

            when(userRepositoryMock.findById(userInDb.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(transactionRepositoryMock.findAllByRelationship_User_UserIdOrderByDateDesc(userInDb.getUserId()))
                    .thenReturn(transactionList);

            //THEN
            List<TransactionDTO> transactionDTOList = transactionService.getAllTransactionsForUser(userInDb.getUserId());
            assertThat(transactionDTOList).isEmpty();

            verify(userRepositoryMock, Mockito.times(1)).findById(userInDb.getUserId());
            verify(transactionRepositoryMock, Mockito.times(1))
                    .findAllByRelationship_User_UserIdOrderByDateDesc(userInDb.getUserId());
        }


        @Test
        @DisplayName("GIVEN an unknown user " +
                "WHEN getting all the transactions for this user " +
                "THEN an PMB Exception is thrown")
        void getAllTransactionsForUser_WithUnknownUser() {
            //GIVEN
            when(userRepositoryMock.findById(UserTestConstants.UNKNOWN_USER_ID))
                    .thenReturn(Optional.empty());

            //THEN
            Exception exception =
                    assertThrows(PMBException.class,
                            () -> transactionService.getAllTransactionsForUser(UserTestConstants.UNKNOWN_USER_ID));
            assertEquals(PMBExceptionConstants.DOES_NOT_EXISTS_USER, exception.getMessage());

            verify(userRepositoryMock, Mockito.times(1))
                    .findById(UserTestConstants.UNKNOWN_USER_ID);
            verify(transactionRepositoryMock, Mockito.times(0))
                    .findAllByRelationship_User_UserIdOrderByDateDesc(UserTestConstants.UNKNOWN_USER_ID);
        }


        @Test
        @DisplayName("GIVEN an null userId " +
                "WHEN getting all the transactions for this user " +
                "THEN an PMB Exception is thrown")
        void getAllTransactionsForUser_WithNullUserId() {
            //THEN
            Exception exception =
                    assertThrows(PMBException.class,
                            () -> transactionService.getAllTransactionsForUser(null));
            assertEquals(PMBExceptionConstants.MISSING_INFORMATION_LIST_TRANSACTION, exception.getMessage());

            verify(userRepositoryMock, Mockito.times(0))
                    .findById(anyLong());
            verify(transactionRepositoryMock, Mockito.times(0))
                    .findAllByRelationship_User_UserIdOrderByDateDesc(anyLong());
        }
    }
}