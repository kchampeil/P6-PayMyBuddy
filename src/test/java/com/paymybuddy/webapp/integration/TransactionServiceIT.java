package com.paymybuddy.webapp.integration;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.TransactionDTO;
import com.paymybuddy.webapp.model.Relationship;
import com.paymybuddy.webapp.model.Transaction;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.RelationshipRepository;
import com.paymybuddy.webapp.repository.TransactionRepository;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.ITransactionService;
import com.paymybuddy.webapp.testconstants.TransactionTestConstants;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import com.paymybuddy.webapp.util.DateUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest
public class TransactionServiceIT {

    @Autowired
    ITransactionService transactionService;

    @Autowired
    TransactionRepository transactionRepository;

    @Autowired
    RelationshipRepository relationshipRepository;

    @Autowired
    UserRepository userRepository;

    private static final DateUtil dateUtil = new DateUtil();

    private User existingUser;
    private User existingFriend;
    private Relationship existingRelationship;
    private TransactionDTO transactionDTOToCreate;


    @BeforeEach
    private void setUpPerTest() {
        //initialisation avec un user et un user ami associé en base
        existingUser = new User();
        existingUser.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
        existingUser.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
        existingUser.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
        existingUser.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
        existingUser.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
        existingUser = userRepository.save(existingUser);


        existingFriend = new User();
        existingFriend.setEmail(UserTestConstants.EXISTING_USER_AS_FRIEND_EMAIL);
        existingFriend.setFirstname(UserTestConstants.EXISTING_USER_AS_FRIEND_FIRSTNAME);
        existingFriend.setLastname(UserTestConstants.EXISTING_USER_AS_FRIEND_LASTNAME);
        existingFriend.setPassword(UserTestConstants.EXISTING_USER_AS_FRIEND_PASSWORD);
        existingFriend.setBalance(UserTestConstants.EXISTING_USER_AS_FRIEND_BALANCE);
        existingFriend = userRepository.save(existingFriend);


        existingRelationship = new Relationship();
        existingRelationship.setUser(existingUser);
        existingRelationship.setFriend(existingFriend);
        existingRelationship = relationshipRepository.save(existingRelationship);

        //initialisation de l objet transactionDTOToCreate
        transactionDTOToCreate = new TransactionDTO();
        transactionDTOToCreate.setDate(dateUtil.getCurrentLocalDateTime());
        transactionDTOToCreate.setDescription(TransactionTestConstants.NEW_TRANSACTION_DESCRIPTION);
        transactionDTOToCreate.setAmountFeeExcluded(TransactionTestConstants.NEW_TRANSACTION_AMOUNT_FEE_EXCLUDED);
        transactionDTOToCreate.setRelationshipId(existingRelationship.getRelationshipId());
    }

    @AfterEach
    private void tearDownPerTest() {
        //nettoyage la DB en fin de test en supprimant les users et la relation créés à l initialisation
        userRepository.deleteById(existingUser.getUserId());
        userRepository.deleteById(existingFriend.getUserId());
        relationshipRepository.deleteById(existingRelationship.getRelationshipId());
    }


    @Nested
    @DisplayName("transferToFriend IT")
    class transferToFriendIT {

        @Test
        @DisplayName("WHEN creating a new transaction to a friend " +
                "with correct informations for an existing relationship " +
                "THEN the returned value is the added transaction, " +
                "AND the transaction is added in DB " +
                "AND the user's balance is decreased by the transaction (amount+fees) amount " +
                "AND the friend's balance is increased byt the transaction amount")
        public void transferToFriendIT_WithSuccess() throws Exception {

            Optional<TransactionDTO> transactionDTOCreated = transactionService.transferToFriend(transactionDTOToCreate);
            assertThat(transactionDTOCreated).isPresent();
            assertNotNull(transactionDTOCreated.get().getTransactionId()); //TODO à ajouter dans les autres tests IT

            Optional<Transaction> transactionCreated = transactionRepository
                    .findById(transactionDTOCreated.get().getTransactionId());
            assertThat(transactionCreated).isPresent();
            assertEquals(transactionDTOToCreate.getDescription(), transactionCreated.get().getDescription());
            //TODO assertEquals(false, transactionCreated.get().getFeeBilled());

            Optional<User> userUpdated = userRepository.findById(existingUser.getUserId());
            assertThat(userUpdated).isPresent();
            assertEquals(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE
                            .subtract(transactionDTOCreated.get().getAmountFeeExcluded())
                            .subtract(transactionDTOCreated.get().getFeeAmount()),
                    userUpdated.get().getBalance());

            Optional<User> friendUpdated = userRepository.findById(existingFriend.getUserId());
            assertThat(friendUpdated).isPresent();
            assertEquals(UserTestConstants.EXISTING_USER_AS_FRIEND_BALANCE
                            .add(transactionDTOCreated.get().getAmountFeeExcluded()),
                    friendUpdated.get().getBalance());

            //nettoyage de la DB en fin de test en supprimant le compte bancaire créé par le test
            transactionRepository.deleteById(transactionCreated.get().getTransactionId());
        }


        @Test
        @DisplayName("WHEN creating a new transaction to a friend " +
                "with correct informations but insufficient user's balance " +
                "THEN an PMBException is thrown AND the transaction is not added in DB " +
                "AND no user balance is updated")
        public void transferToFriendIT_WithInsufficientBalance() {
            //initialisation du test avec un solde très faible
            existingUser.setBalance(UserTestConstants.EXISTING_USER_WITH_LOW_BALANCE);
            existingUser = userRepository.save(existingUser);

            //test
            Exception exception = assertThrows(PMBException.class,
                    () -> transactionService.transferToFriend(transactionDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.INSUFFICIENT_BALANCE);

            Optional<Transaction> transactionCreated = transactionRepository
                    .findByDateAndRelationship_RelationshipId(
                            transactionDTOToCreate.getDate(), transactionDTOToCreate.getRelationshipId());
            assertThat(transactionCreated).isNotPresent();

            Optional<User> userNotUpdated = userRepository.findById(existingUser.getUserId());
            assertThat(userNotUpdated).isPresent();
            assertEquals(existingUser.getBalance(), userNotUpdated.get().getBalance());

            Optional<User> friendNotUpdated = userRepository.findById(existingFriend.getUserId());
            assertThat(friendNotUpdated).isPresent();
            assertEquals(existingFriend.getBalance().setScale(2, RoundingMode.HALF_UP)
                    , friendNotUpdated.get().getBalance());
        }
    }


    @Test
    @DisplayName("WHEN getting the list of transactions for an existing user " +
            "THEN the list of transactions in DB is returned")
    public void getAllTransactionsForUser_WithData() throws PMBException {

        //initialisation du test avec un transfert bancaire en base
        Transaction existingTransaction = new Transaction();
        existingTransaction.setDate(dateUtil.getCurrentLocalDateTime());
        existingTransaction.setDescription(TransactionTestConstants.EXISTING_TRANSACTION_DESCRIPTION);
        existingTransaction.setAmountFeeExcluded(TransactionTestConstants.EXISTING_TRANSACTION_AMOUNT_FEE_EXCLUDED);
        existingTransaction.setFeeAmount(TransactionTestConstants.EXISTING_TRANSACTION_FEE_AMOUNT);
        existingTransaction.setFeeBilled(false);
        existingTransaction.setRelationship(existingRelationship);
        existingTransaction = transactionRepository.save(existingTransaction);

        //test
        List<TransactionDTO> transactionDTOList = transactionService.getAllTransactionsForUser(existingUser.getUserId());

        assertThat(transactionDTOList).isNotEmpty();
        assertEquals(existingTransaction.getTransactionId(), transactionDTOList.get(0).getTransactionId());

        //nettoyage de la DB en fin de test en supprimant le compte bancaire créé par le test
        transactionRepository.deleteById(existingTransaction.getTransactionId());
    }
}
