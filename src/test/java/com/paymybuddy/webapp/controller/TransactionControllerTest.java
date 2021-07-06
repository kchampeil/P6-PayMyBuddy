package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.TransactionDTO;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.service.PMBUserDetailsService;
import com.paymybuddy.webapp.service.contract.IRelationshipService;
import com.paymybuddy.webapp.service.contract.ITransactionService;
import com.paymybuddy.webapp.testconstants.RelationshipTestConstants;
import com.paymybuddy.webapp.testconstants.TransactionTestConstants;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = TransactionController.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ITransactionService transactionServiceMock;

    @MockBean
    private IRelationshipService relationshipServiceMock;

    @MockBean
    private PMBUserDetailsService pmbUserDetailsServiceMock;

    @MockBean
    private PasswordEncoder passwordEncoderMock;

    private static User userInDb;

    @BeforeAll
    private static void setUp() {
        userInDb = new User();
        userInDb.setUserId(UserTestConstants.EXISTING_USER_ID);
        userInDb.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
        userInDb.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
        userInDb.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
        userInDb.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
        userInDb.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
    }

    @Nested
    @DisplayName("showHomeTransaction tests")
    class ShowHomeTransactionTest {

        @WithMockUser
        @Test
        @DisplayName("WHEN asking for the transfer page while logged in " +
                " THEN return status is ok and the expected view is the transfer page")
        void showHomeTransactionTest_LoggedIn() throws Exception {
            //GIVEN
            when(pmbUserDetailsServiceMock.getCurrentUser()).thenReturn(userInDb);

            //THEN
            mockMvc.perform(get("/transfer"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("transactionDTO"))
                    .andExpect(model().attributeExists("transactionDTOList"))
                    .andExpect(view().name(ViewNameConstants.TRANSACTION_HOME));

            verify(pmbUserDetailsServiceMock, Mockito.times(1))
                    .getCurrentUser();
            verify(relationshipServiceMock, Mockito.times(1))
                    .getAllRelationshipsForUser(userInDb.getUserId());
            verify(transactionServiceMock, Mockito.times(1))
                    .getAllTransactionsForUser(userInDb.getUserId());
        }


        @Test
        @DisplayName("WHEN asking for the transfer page while not logged in " +
                " THEN return status is 302 and the expected view is the login page")
        void showHomeRelationshipTest_NotLoggedIn() throws Exception {
            mockMvc.perform(get("/transfer"))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrlPattern("**/" + ViewNameConstants.USER_LOGIN));

            verify(pmbUserDetailsServiceMock, Mockito.times(0))
                    .getCurrentUser();
            verify(relationshipServiceMock, Mockito.times(0))
                    .getAllRelationshipsForUser(userInDb.getUserId());
            verify(transactionServiceMock, Mockito.times(0))
                    .getAllTransactionsForUser(userInDb.getUserId());
        }
    }


    @Nested
    @DisplayName("addTransaction tests")
    class AddTransactionTest {

        @WithMockUser
        @Test
        @DisplayName("GIVEN a new transaction to add " +
                "WHEN processing a POST /transfer request for this transaction " +
                "THEN return status is ok " +
                "AND the expected view is the transfer page with transaction list updated")
        void addTransactionTest_WithSuccess() throws Exception {
            //GIVEN
            when(pmbUserDetailsServiceMock.getCurrentUser()).thenReturn(userInDb);

            TransactionDTO transactionDTOAdded = new TransactionDTO();
            transactionDTOAdded.setRelationshipId(RelationshipTestConstants.EXISTING_RELATIONSHIP_ID);
            transactionDTOAdded.setAmountFeeExcluded(TransactionTestConstants.NEW_TRANSACTION_AMOUNT_FEE_EXCLUDED);
            transactionDTOAdded.setDescription(TransactionTestConstants.NEW_TRANSACTION_DESCRIPTION);
            transactionDTOAdded.setFeeAmount(TransactionTestConstants.NEW_TRANSACTION_FEE_AMOUNT);

            when(transactionServiceMock.transferToFriend(any(TransactionDTO.class)))
                    .thenReturn(Optional.of(transactionDTOAdded));

            //THEN
            mockMvc.perform(post("/transfer")
                    .param("relationshipId", transactionDTOAdded.getRelationshipId().toString())
                    .param("amountFeeExcluded", transactionDTOAdded.getAmountFeeExcluded().toString())
                    .param("description", transactionDTOAdded.getDescription())
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("transactionDTO"))
                    .andExpect(model().attributeExists("transactionDTOList"))
                    .andExpect(view().name(ViewNameConstants.TRANSACTION_HOME));

            verify(pmbUserDetailsServiceMock, Mockito.times(1))
                    .getCurrentUser();
            verify(relationshipServiceMock, Mockito.times(1))
                    .getAllRelationshipsForUser(userInDb.getUserId());
            verify(transactionServiceMock, Mockito.times(1))
                    .getAllTransactionsForUser(userInDb.getUserId());
            verify(transactionServiceMock, Mockito.times(1))
                    .transferToFriend(any(TransactionDTO.class));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a new transaction to add with missing description " +
                "WHEN processing a POST /transfer request for this transaction " +
                "THEN the returned code is ok " +
                "AND the expected view is the transfer page filled with entered transaction")
        void addTransactionTest_WithMissingInformation() throws Exception {
            //GIVEN
            when(pmbUserDetailsServiceMock.getCurrentUser()).thenReturn(userInDb);

            when(transactionServiceMock.transferToFriend(any(TransactionDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_TRANSACTION));

            //THEN
            mockMvc.perform(post("/transfer")
                    .param("relationshipId", RelationshipTestConstants.EXISTING_RELATIONSHIP_ID.toString())
                    .param("amountFeeExcluded",
                            TransactionTestConstants.NEW_TRANSACTION_AMOUNT_FEE_EXCLUDED.toString())
                    .param("description", "")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("transactionDTO"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("transactionDTO", "description"))
                    .andExpect(view().name(ViewNameConstants.TRANSACTION_HOME));

            verify(pmbUserDetailsServiceMock, Mockito.times(1))
                    .getCurrentUser();
            verify(relationshipServiceMock, Mockito.times(1))
                    .getAllRelationshipsForUser(userInDb.getUserId());
            verify(transactionServiceMock, Mockito.times(1))
                    .getAllTransactionsForUser(userInDb.getUserId());
            verify(transactionServiceMock, Mockito.times(0))
                    .transferToFriend(any(TransactionDTO.class));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a transaction with non existing relationship " +
                "WHEN processing a POST /transfer request for this transaction " +
                "THEN the returned code is ok " +
                "AND the expected view is the transfer page filled with transaction " +
                "AND an 'does not exist' error is shown")
        void addTransactionTest_WithNonExistingRelationship() throws Exception {
            //GIVEN
            when(pmbUserDetailsServiceMock.getCurrentUser()).thenReturn(userInDb);

            when(transactionServiceMock.transferToFriend(any(TransactionDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_RELATIONSHIP));

            //THEN
            mockMvc.perform(post("/transfer")
                    .param("relationshipId",
                            RelationshipTestConstants.UNKNOWN_RELATIONSHIP_ID.toString())
                    .param("amountFeeExcluded",
                            TransactionTestConstants.NEW_TRANSACTION_AMOUNT_FEE_EXCLUDED.toString())
                    .param("description", TransactionTestConstants.NEW_TRANSACTION_DESCRIPTION)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("transactionDTO"))
                    .andExpect(model().attributeHasFieldErrorCode(
                            "transactionDTO",
                            "relationshipId",
                            "transfer.TransactionDTO.relationshipId.doesNotExist"))
                    .andExpect(view().name(ViewNameConstants.TRANSACTION_HOME));

            verify(pmbUserDetailsServiceMock, Mockito.times(1))
                    .getCurrentUser();
            verify(relationshipServiceMock, Mockito.times(1))
                    .getAllRelationshipsForUser(userInDb.getUserId());
            verify(transactionServiceMock, Mockito.times(1))
                    .getAllTransactionsForUser(userInDb.getUserId());
            verify(transactionServiceMock, Mockito.times(1))
                    .transferToFriend(any(TransactionDTO.class));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a transaction with an amount greater than user balance " +
                "WHEN processing a POST /transfer request for this transaction " +
                "THEN the returned code is ok " +
                "AND the expected view is the transfer page filled with transaction " +
                "AND an 'insufficient balance' error is shown")
        void addTransactionTest_WithInsufficientBalance() throws Exception {
            //GIVEN
            when(pmbUserDetailsServiceMock.getCurrentUser()).thenReturn(userInDb);

            when(transactionServiceMock.transferToFriend(any(TransactionDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.INSUFFICIENT_BALANCE));

            //THEN
            mockMvc.perform(post("/transfer")
                    .param("relationshipId",
                            RelationshipTestConstants.EXISTING_RELATIONSHIP_ID.toString())
                    .param("amountFeeExcluded",
                            TransactionTestConstants.NEW_TRANSACTION_AMOUNT_FEE_EXCLUDED.toString())
                    .param("description", TransactionTestConstants.NEW_TRANSACTION_DESCRIPTION)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("transactionDTO"))
                    .andExpect(model().attributeHasFieldErrorCode(
                            "transactionDTO",
                            "amountFeeExcluded",
                            "transfer.TransactionDTO.amountFeeExcluded.insufficientBalance"))
                    .andExpect(view().name(ViewNameConstants.TRANSACTION_HOME));

            verify(pmbUserDetailsServiceMock, Mockito.times(1))
                    .getCurrentUser();
            verify(relationshipServiceMock, Mockito.times(1))
                    .getAllRelationshipsForUser(userInDb.getUserId());
            verify(transactionServiceMock, Mockito.times(1))
                    .getAllTransactionsForUser(userInDb.getUserId());
            verify(transactionServiceMock, Mockito.times(1))
                    .transferToFriend(any(TransactionDTO.class));
        }
    }
}