package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.BankTransferTypes;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.BankTransferDTO;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.service.PMBUserDetailsService;
import com.paymybuddy.webapp.service.contract.IBankAccountService;
import com.paymybuddy.webapp.service.contract.IBankTransferService;
import com.paymybuddy.webapp.testconstants.BankAccountTestConstants;
import com.paymybuddy.webapp.testconstants.BankTransferTestConstants;
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

@WebMvcTest(controllers = BankTransferController.class)
class BankTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IBankTransferService bankTransferServiceMock;

    @MockBean
    private IBankAccountService bankAccountServiceMock;

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
    @DisplayName("showHomeBankTransfer tests")
    class ShowHomeBankTransferTest {

        @WithMockUser
        @Test
        @DisplayName("WHEN asking for the profile page while logged in " +
                " THEN return status is ok and the expected view is the profile page")
        void showHomeBankTransferTest_LoggedIn() throws Exception {
            //GIVEN
            when(pmbUserDetailsServiceMock.getCurrentUser()).thenReturn(userInDb);

            mockMvc.perform(get("/profile"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankTransferDTO"))
                    .andExpect(model().attributeExists("bankTransferDTOList"))
                    .andExpect(view().name(ViewNameConstants.BANK_TRANSFER_HOME));

            verify(pmbUserDetailsServiceMock, Mockito.times(1))
                    .getCurrentUser();
            verify(bankAccountServiceMock, Mockito.times(1))
                    .getAllBankAccountsForUser(userInDb.getUserId());
            verify(bankTransferServiceMock, Mockito.times(1))
                    .getAllBankTransfersForUser(userInDb.getUserId());
        }


        @Test
        @DisplayName("WHEN asking for the profile page while not logged in " +
                " THEN return status is 302 and the expected view is the login page")
        void showHomeBankTransferTest_NotLoggedIn() throws Exception {
            mockMvc.perform(get("/profile"))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrlPattern("**/" + ViewNameConstants.USER_LOGIN));

            verify(pmbUserDetailsServiceMock, Mockito.times(0))
                    .getCurrentUser();
            verify(bankAccountServiceMock, Mockito.times(0))
                    .getAllBankAccountsForUser(userInDb.getUserId());
            verify(bankTransferServiceMock, Mockito.times(0))
                    .getAllBankTransfersForUser(userInDb.getUserId());
        }
    }


    @Nested
    @DisplayName("addBankTransfer tests")
    class AddBankTransferTest {

        @WithMockUser
        @Test
        @DisplayName("GIVEN a new bank transfer to add " +
                "WHEN processing a POST /addBankTransfer request for this bank transfer " +
                "THEN return status is ok " +
                "AND the expected view is the profile page with bank transfer list updated")
        void addBankTransferTest_WithSuccess() throws Exception {
            //GIVEN
            when(pmbUserDetailsServiceMock.getCurrentUser()).thenReturn(userInDb);

            BankTransferDTO bankTransferDTOAdded = new BankTransferDTO();
            bankTransferDTOAdded.setBankAccountId(BankAccountTestConstants.EXISTING_BANK_ACCOUNT_ID);
            bankTransferDTOAdded.setAmount(BankTransferTestConstants.NEW_BANK_TRANSFER_AMOUNT);
            bankTransferDTOAdded.setType(BankTransferTypes.DEBIT);
            bankTransferDTOAdded.setDescription(BankTransferTestConstants.NEW_BANK_TRANSFER_DESCRIPTION);

            when(bankTransferServiceMock.transferWithBankAccount(any(BankTransferDTO.class)))
                    .thenReturn(Optional.of(bankTransferDTOAdded));

            //THEN
            mockMvc.perform(post("/profile")
                    .param("bankAccountId", bankTransferDTOAdded.getBankAccountId().toString())
                    .param("amount", bankTransferDTOAdded.getAmount().toString())
                    .param("type", bankTransferDTOAdded.getType().toString())
                    .param("description", bankTransferDTOAdded.getDescription())
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankTransferDTO"))
                    .andExpect(model().attributeExists("bankTransferDTOList"))
                    .andExpect(view().name(ViewNameConstants.BANK_TRANSFER_HOME));

            verify(pmbUserDetailsServiceMock, Mockito.times(1))
                    .getCurrentUser();
            verify(bankAccountServiceMock, Mockito.times(1))
                    .getAllBankAccountsForUser(userInDb.getUserId());
            verify(bankTransferServiceMock, Mockito.times(1))
                    .getAllBankTransfersForUser(userInDb.getUserId());
            verify(bankTransferServiceMock, Mockito.times(1))
                    .transferWithBankAccount(any(BankTransferDTO.class));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a new bank transfer to add with missing description " +
                "WHEN processing a POST /addBankTransfer request for this bank transfer " +
                "THEN the returned code is ok " +
                "AND the expected view is the profile page filled with entered bank transfer")
        void addBankTransferTest_WithMissingInformation() throws Exception {
            //GIVEN
            when(pmbUserDetailsServiceMock.getCurrentUser()).thenReturn(userInDb);

            when(bankTransferServiceMock.transferWithBankAccount(any(BankTransferDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_TRANSFER));

            //THEN
            mockMvc.perform(post("/profile")
                    .param("bankAccountId", BankAccountTestConstants.EXISTING_BANK_ACCOUNT_ID.toString())
                    .param("amount", BankTransferTestConstants.NEW_BANK_TRANSFER_AMOUNT.toString())
                    .param("type", BankTransferTypes.DEBIT.toString())
                    .param("description", "")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankTransferDTO"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("bankTransferDTO", "description"))
                    .andExpect(view().name(ViewNameConstants.BANK_TRANSFER_HOME));

            verify(pmbUserDetailsServiceMock, Mockito.times(1))
                    .getCurrentUser();
            verify(bankAccountServiceMock, Mockito.times(1))
                    .getAllBankAccountsForUser(userInDb.getUserId());
            verify(bankTransferServiceMock, Mockito.times(1))
                    .getAllBankTransfersForUser(userInDb.getUserId());
            verify(bankTransferServiceMock, Mockito.times(0))
                    .transferWithBankAccount(any(BankTransferDTO.class));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a bank transfer with non existing bank account " +
                "WHEN processing a POST /addBankTransfer request for this bank transfer " +
                "THEN the returned code is ok " +
                "AND the expected view is the profile page filled with bank transfer " +
                "AND an 'does not exist' error is shown")
        void addBankTransferTest_WithNonExistingBankAccount() throws Exception {
            //GIVEN
            when(pmbUserDetailsServiceMock.getCurrentUser()).thenReturn(userInDb);

            when(bankTransferServiceMock.transferWithBankAccount(any(BankTransferDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_BANK_ACCOUNT));

            //THEN
            mockMvc.perform(post("/profile")
                    .param("bankAccountId", BankAccountTestConstants.UNKNOWN_BANK_ACCOUNT_ID.toString())
                    .param("amount", BankTransferTestConstants.NEW_BANK_TRANSFER_AMOUNT.toString())
                    .param("type", BankTransferTypes.DEBIT.toString())
                    .param("description", BankTransferTestConstants.NEW_BANK_TRANSFER_DESCRIPTION)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankTransferDTO"))
                    .andExpect(model().attributeHasFieldErrorCode("bankTransferDTO",
                            "bankAccountId", "profile.BankTransferDTO.bankAccountId.doesNotExist"))
                    .andExpect(view().name(ViewNameConstants.BANK_TRANSFER_HOME));

            verify(pmbUserDetailsServiceMock, Mockito.times(1))
                    .getCurrentUser();
            verify(bankAccountServiceMock, Mockito.times(1))
                    .getAllBankAccountsForUser(userInDb.getUserId());
            verify(bankTransferServiceMock, Mockito.times(1))
                    .getAllBankTransfersForUser(userInDb.getUserId());
            verify(bankTransferServiceMock, Mockito.times(1))
                    .transferWithBankAccount(any(BankTransferDTO.class));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a bank transfer from PMB to bank with an amount greater than user balance " +
                "WHEN processing a POST /addBankTransfer request for this bank transfer " +
                "THEN the returned code is ok " +
                "AND the expected view is the profile page filled with bank transfer " +
                "AND an 'insufficient balance' error is shown")
        void addBankTransferTest_WithInsufficientBalance() throws Exception {
            //GIVEN
            when(pmbUserDetailsServiceMock.getCurrentUser()).thenReturn(userInDb);

            when(bankTransferServiceMock.transferWithBankAccount(any(BankTransferDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.INSUFFICIENT_BALANCE));

            //THEN
            mockMvc.perform(post("/profile")
                    .param("bankAccountId", BankAccountTestConstants.EXISTING_BANK_ACCOUNT_ID.toString())
                    .param("amount", BankTransferTestConstants.NEW_BANK_TRANSFER_AMOUNT.toString())
                    .param("type", BankTransferTypes.DEBIT.toString())
                    .param("description", BankTransferTestConstants.NEW_BANK_TRANSFER_DESCRIPTION)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankTransferDTO"))
                    .andExpect(model().attributeHasFieldErrorCode("bankTransferDTO",
                            "amount", "profile.BankTransferDTO.amount.insufficientBalance"))
                    .andExpect(view().name(ViewNameConstants.BANK_TRANSFER_HOME));

            verify(pmbUserDetailsServiceMock, Mockito.times(1))
                    .getCurrentUser();
            verify(bankAccountServiceMock, Mockito.times(1))
                    .getAllBankAccountsForUser(userInDb.getUserId());
            verify(bankTransferServiceMock, Mockito.times(1))
                    .getAllBankTransfersForUser(userInDb.getUserId());
            verify(bankTransferServiceMock, Mockito.times(1))
                    .transferWithBankAccount(any(BankTransferDTO.class));
        }
    }
}