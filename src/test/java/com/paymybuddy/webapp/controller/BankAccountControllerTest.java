package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.BankAccountDTO;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.service.PMBUserDetailsService;
import com.paymybuddy.webapp.service.contract.IBankAccountService;
import com.paymybuddy.webapp.service.contract.IUserService;
import com.paymybuddy.webapp.testconstants.BankAccountTestConstants;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@WebMvcTest(controllers = BankAccountController.class)
class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IBankAccountService bankAccountServiceMock;

    @MockBean
    private IUserService userServiceMock;

    @MockBean
    private PMBUserDetailsService pmbUserDetailsServiceMock;

    @MockBean
    private PasswordEncoder passwordEncoderMock;

    private static UserDTO userInDb;

    @BeforeAll
    private static void setUp() {
        userInDb = new UserDTO();
        userInDb.setUserId(UserTestConstants.EXISTING_USER_ID);
        userInDb.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
        userInDb.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
        userInDb.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
        userInDb.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
        userInDb.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
    }

    @Nested
    @DisplayName("showHomeBankAccount tests")
    class ShowHomeBankAccountTest {

        @WithMockUser
        @Test
        @DisplayName("WHEN asking for the bank account page while logged in " +
                " THEN return status is ok and the expected view is the bank account page")
        void showHomeBankAccountTest_LoggedIn() throws Exception {
            //GIVEN
            when(userServiceMock.getUserDTOByEmail(anyString())).thenReturn(userInDb);

            //THEN
            mockMvc.perform(get("/addBankAccount"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankAccountDTO"))
                    .andExpect(model().attributeExists("bankAccountDTOList"))
                    .andExpect(view().name(ViewNameConstants.BANK_ACCOUNT_HOME));

            verify(userServiceMock, Mockito.times(1))
                    .getUserDTOByEmail(anyString());
            verify(bankAccountServiceMock, Mockito.times(1))
                    .getAllBankAccountsForUser(userInDb.getUserId());
        }


        @Test
        @DisplayName("WHEN asking for the bank account page while not logged in " +
                " THEN return status is Found (302) and the expected view is the login page")
        void showHomeBankAccountTest_NotLoggedIn() throws Exception {
            mockMvc.perform(get("/addBankAccount"))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrlPattern("**/" + ViewNameConstants.USER_LOGIN));

            verify(userServiceMock, Mockito.times(0))
                    .getUserDTOByEmail(anyString());
            verify(bankAccountServiceMock, Mockito.times(0))
                    .getAllBankAccountsForUser(userInDb.getUserId());
        }
    }


    @Nested
    @DisplayName("addBankAccount tests")
    class AddBankAccountTest {

        @WithMockUser
        @Test
        @DisplayName("GIVEN a new bank account to add " +
                "WHEN processing a POST /addBankAccount request for this bank account " +
                "THEN return status is ok " +
                "AND the expected view is the bank account page with bank account list updated")
        void addBankAccountTest_WithSuccess() throws Exception {
            //GIVEN
            when(userServiceMock.getUserDTOByEmail(anyString())).thenReturn(userInDb);

            BankAccountDTO bankAccountDTOAdded = new BankAccountDTO();
            bankAccountDTOAdded.setUserId(UserTestConstants.EXISTING_USER_ID);
            bankAccountDTOAdded.setIban(BankAccountTestConstants.NEW_BANK_ACCOUNT_IBAN);
            bankAccountDTOAdded.setName(BankAccountTestConstants.NEW_BANK_ACCOUNT_NAME);

            when(bankAccountServiceMock.createBankAccount(any(BankAccountDTO.class)))
                    .thenReturn(Optional.of(bankAccountDTOAdded));

            //THEN
            mockMvc.perform(post("/addBankAccount")
                    .param("iban", bankAccountDTOAdded.getIban())
                    .param("name", bankAccountDTOAdded.getName())
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankAccountDTO"))
                    .andExpect(model().attributeExists("bankAccountDTOList"))
                    .andExpect(view().name(ViewNameConstants.BANK_ACCOUNT_HOME));

            verify(userServiceMock, Mockito.times(1))
                    .getUserDTOByEmail(anyString());
            verify(bankAccountServiceMock, Mockito.times(1))
                    .createBankAccount(any(BankAccountDTO.class));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a new bank account to add with missing name" +
                "WHEN processing a POST /addBankAccount request for this bank account " +
                "THEN the returned code is ok " +
                "AND the expected view is the bank account page filled with entered bank account")
        void addBankAccountTest_WithMissingInformation() throws Exception {
            //GIVEN
            when(userServiceMock.getUserDTOByEmail(anyString())).thenReturn(userInDb);

            when(bankAccountServiceMock.createBankAccount(any(BankAccountDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_ACCOUNT));

            //THEN
            mockMvc.perform(post("/addBankAccount")
                    .param("iban", BankAccountTestConstants.NEW_BANK_ACCOUNT_IBAN)
                    .param("name", "")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankAccountDTO"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("bankAccountDTO", "name"))
                    .andExpect(view().name(ViewNameConstants.BANK_ACCOUNT_HOME));

            verify(userServiceMock, Mockito.times(1))
                    .getUserDTOByEmail(anyString());
            verify(bankAccountServiceMock, Mockito.times(0))
                    .createBankAccount(any(BankAccountDTO.class));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a bank account already present in bank account list " +
                "WHEN processing a POST /addBankAccount request for this bank account " +
                "THEN the returned code is ok " +
                "AND the expected view is the bank account page filled with bank account " +
                "AND an 'already exists' error is shown")
        void addBankAccountTest_WithAlreadyExistingBankAccount() throws Exception {
            //GIVEN
            when(userServiceMock.getUserDTOByEmail(anyString())).thenReturn(userInDb);

            when(bankAccountServiceMock.createBankAccount(any(BankAccountDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.ALREADY_EXIST_BANK_ACCOUNT));

            //THEN
            mockMvc.perform(post("/addBankAccount")
                    .param("iban", BankAccountTestConstants.EXISTING_BANK_ACCOUNT_IBAN)
                    .param("name", BankAccountTestConstants.NEW_BANK_ACCOUNT_NAME)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankAccountDTO"))
                    .andExpect(model().attributeHasFieldErrorCode(
                            "bankAccountDTO",
                            "iban",
                            "addBankAccount.BankAccountDTO.iban.alreadyExists"))
                    .andExpect(view().name(ViewNameConstants.BANK_ACCOUNT_HOME));

            verify(userServiceMock, Mockito.times(1))
                    .getUserDTOByEmail(anyString());
            verify(bankAccountServiceMock, Mockito.times(1))
                    .createBankAccount(any(BankAccountDTO.class));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a bank account with an invalid iban " +
                "WHEN processing a POST /addBankAccount request for this bank account " +
                "THEN the returned code is ok " +
                "AND the expected view is the bank account page filled with bank account " +
                "AND an 'invalid iban' error is shown")
        void addBankAccountTest_WithInvalidIban() throws Exception {
            //GIVEN
            when(userServiceMock.getUserDTOByEmail(anyString())).thenReturn(userInDb);

            when(bankAccountServiceMock.createBankAccount(any(BankAccountDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.INVALID_IBAN));

            //THEN
            mockMvc.perform(post("/addBankAccount")
                    .param("iban", BankAccountTestConstants.NEW_BANK_ACCOUNT_INVALID_IBAN)
                    .param("name", BankAccountTestConstants.NEW_BANK_ACCOUNT_NAME)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankAccountDTO"))
                    .andExpect(model().attributeHasFieldErrorCode(
                            "bankAccountDTO",
                            "iban",
                            "addBankAccount.BankAccountDTO.iban.invalid"))
                    .andExpect(view().name(ViewNameConstants.BANK_ACCOUNT_HOME));

            verify(userServiceMock, Mockito.times(1))
                    .getUserDTOByEmail(anyString());
            verify(bankAccountServiceMock, Mockito.times(1))
                    .createBankAccount(any(BankAccountDTO.class));
        }
    }
}