package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.BankAccountDTO;
import com.paymybuddy.webapp.service.contract.IBankAccountService;
import com.paymybuddy.webapp.testconstants.BankAccountTestConstants;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@WebMvcTest(controllers = BankAccountController.class)
class BankAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IBankAccountService bankAccountServiceMock;

    @Test
    @DisplayName("WHEN asking for the contact page" +
            " THEN return status is ok and the expected view is the contact page")
    void showHomeBankAccount() throws Exception {
        mockMvc.perform(get("/addBankAccount"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("bankAccountDTO"))
                .andExpect(model().attributeExists("bankAccountDTOList"))
                .andExpect(view().name(ViewNameConstants.BANK_ACCOUNT_HOME));
    }


    @Nested
    @DisplayName("addBankAccount tests")
    class AddBankAccountTest {
        @Test
        @DisplayName("GIVEN a new bank account to add " +
                "WHEN processing a POST /addBankAccount request for this bank account " +
                "THEN return status is ok " +
                "AND the expected view is the contact page with bank account list updated")
        void addBankAccountTest_WithSuccess() throws Exception {
            //GIVEN
            BankAccountDTO bankAccountDTOAdded = new BankAccountDTO();
            bankAccountDTOAdded.setUserId(UserTestConstants.EXISTING_USER_ID);
            bankAccountDTOAdded.setIban(BankAccountTestConstants.NEW_BANK_ACCOUNT_IBAN);
            bankAccountDTOAdded.setName(BankAccountTestConstants.NEW_BANK_ACCOUNT_NAME);

            when(bankAccountServiceMock.createBankAccount(any(BankAccountDTO.class)))
                    .thenReturn(Optional.of(bankAccountDTOAdded));

            //THEN
            mockMvc.perform(post("/addBankAccount")
                    .param("iban", bankAccountDTOAdded.getIban())
                    .param("name", bankAccountDTOAdded.getName()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankAccountDTO"))
                    .andExpect(model().attributeExists("bankAccountDTOList"))
                    .andExpect(view().name(ViewNameConstants.BANK_ACCOUNT_HOME));
        }


        @Test
        @DisplayName("GIVEN a new bank account to add with missing name" +
                "WHEN processing a POST /addBankAccount request for this bank account " +
                "THEN the returned code is ok " +
                "AND the expected view is the bank account page filled with entered bank account")
        void addBankAccountTest_WithMissingInformation() throws Exception {
            //GIVEN
            when(bankAccountServiceMock.createBankAccount(any(BankAccountDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_ACCOUNT));

            //THEN
            mockMvc.perform(post("/addBankAccount")
                    .param("iban", BankAccountTestConstants.NEW_BANK_ACCOUNT_IBAN)
                    .param("name", ""))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankAccountDTO"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("bankAccountDTO", "name"))
                    .andExpect(view().name(ViewNameConstants.BANK_ACCOUNT_HOME));
        }


        @Test
        @DisplayName("GIVEN a bank account already present in bank account list " +
                "WHEN processing a POST /addBankAccount request for this bank account " +
                "THEN the returned code is ok " +
                "AND the expected view is the bank account page filled with bank account " +
                "AND an 'already exists' error is shown")
        void addBankAccountTest_WithAlreadyExistingBankAccount() throws Exception {
            //GIVEN
            when(bankAccountServiceMock.createBankAccount(any(BankAccountDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.ALREADY_EXIST_BANK_ACCOUNT));

            //THEN
            mockMvc.perform(post("/addBankAccount")
                    .param("iban", BankAccountTestConstants.EXISTING_BANK_ACCOUNT_IBAN)
                    .param("name", BankAccountTestConstants.NEW_BANK_ACCOUNT_NAME))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankAccountDTO"))
                    .andExpect(model().attributeHasFieldErrorCode("bankAccountDTO", "iban", "addBankAccount.BankAccountDTO.iban.alreadyExists"))
                    .andExpect(view().name(ViewNameConstants.BANK_ACCOUNT_HOME));
        }


        @Test
        @DisplayName("GIVEN a bank account with an invalid iban " +
                "WHEN processing a POST /addBankAccount request for this bank account " +
                "THEN the returned code is ok " +
                "AND the expected view is the bank account page filled with bank account " +
                "AND an 'invalid iban' error is shown")
        void addBankAccountTest_WithInvalidIban() throws Exception {
            //GIVEN
            when(bankAccountServiceMock.createBankAccount(any(BankAccountDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.INVALID_IBAN));

            //THEN
            mockMvc.perform(post("/addBankAccount")
                    .param("iban", BankAccountTestConstants.NEW_BANK_ACCOUNT_INVALID_IBAN)
                    .param("name", BankAccountTestConstants.NEW_BANK_ACCOUNT_NAME))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("bankAccountDTO"))
                    .andExpect(model().attributeHasFieldErrorCode("bankAccountDTO", "iban", "addBankAccount.BankAccountDTO.iban.invalid"))
                    .andExpect(view().name(ViewNameConstants.BANK_ACCOUNT_HOME));
        }
    }
}