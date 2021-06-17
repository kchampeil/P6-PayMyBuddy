package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.ErrorCodeConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.service.contract.IUserService;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;


@WebMvcTest(controllers = UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUserService userServiceMock;


    @Test
    @DisplayName("WHEN asking for the registration page" +
            " THEN return status is ok and the expected view is the registration page")
    void showRegistrationFormTest() throws Exception {
        mockMvc.perform(get("/registerUser"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name(ViewNameConstants.USER_REGISTRATION));
    }


    @Nested
    @DisplayName("registerUser tests")
    class RegisterUserTest {

        @Test
        @DisplayName("GIVEN a new user to register " +
                "WHEN processing a POST /registerUser request for this user " +
                "THEN return status is ok " +
                "AND the expected view is the user profile page filled with user information (including id)")
        void registerUserTest_WithSuccess() throws Exception {
            //GIVEN
            UserDTO userDTORegistered = new UserDTO();
            userDTORegistered.setUserId(UserTestConstants.NEW_USER_ID);
            userDTORegistered.setEmail(UserTestConstants.NEW_USER_EMAIL);
            userDTORegistered.setFirstname(UserTestConstants.NEW_USER_FIRSTNAME);
            userDTORegistered.setLastname(UserTestConstants.NEW_USER_LASTNAME);
            userDTORegistered.setPassword(UserTestConstants.NEW_USER_PASSWORD);
            userDTORegistered.setBalance(BigDecimal.ZERO);

            when(userServiceMock.createUser(any(UserDTO.class)))
                    .thenReturn(Optional.of(userDTORegistered));

            //THEN
            mockMvc.perform(post("/registerUser")
                    .param("email", userDTORegistered.getEmail())
                    .param("firstname", userDTORegistered.getFirstname())
                    .param("lastname", userDTORegistered.getLastname())
                    .param("password", userDTORegistered.getPassword()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("user"))
                    .andExpect(model().attribute("user", userDTORegistered))
                    .andExpect(view().name(ViewNameConstants.USER_PROFILE));
        }


        @Test
        @DisplayName("GIVEN a new user to register with missing firstname and password" +
                "WHEN processing a POST /registerUser request for this user " +
                "THEN the returned code is ok " +
                "AND the expected view is the registration form filled with entered user information")
        void registerUserTest_WithMissingInformation() throws Exception {
            //GIVEN
            when(userServiceMock.createUser(any(UserDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_USER));

            //THEN
            mockMvc.perform(post("/registerUser")
                    .param("email", UserTestConstants.NEW_USER_EMAIL)
                    .param("firstname", "")
                    .param("lastname", UserTestConstants.NEW_USER_LASTNAME)
                    .param("password", ""))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("user"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("user", "firstname", "password"))
                    .andExpect(view().name(ViewNameConstants.USER_REGISTRATION));
        }


        @Test
        @DisplayName("GIVEN a user email already present in PMB " +
                "WHEN processing a POST /registerUser request for this user " +
                "THEN the returned code is ok " +
                "AND the expected view is the registration form filled with user information " +
                "AND an 'already exists' error is shown")
            //TODO et un message indiquant que l'utilisateur existe déjà est affiché ?
        void registerUserTest_WithAlreadyExistingEmail() throws Exception {
            //GIVEN
            when(userServiceMock.createUser(any(UserDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.ALREADY_EXIST_USER
                            + UserTestConstants.EXISTING_USER_EMAIL));

            //THEN
            mockMvc.perform(post("/registerUser")
                    .param("email", UserTestConstants.EXISTING_USER_EMAIL)
                    .param("firstname", UserTestConstants.NEW_USER_FIRSTNAME)
                    .param("lastname", UserTestConstants.NEW_USER_LASTNAME)
                    .param("password", UserTestConstants.NEW_USER_PASSWORD))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("user"))
                    .andExpect(model().attributeHasFieldErrorCode("user","email", ErrorCodeConstants.EMAIL_ALREADY_EXISTS))
                    .andExpect(view().name(ViewNameConstants.USER_REGISTRATION));
        }
    }

    @Test
    @DisplayName("WHEN asking for the registration page" +
            " THEN return status is ok and the expected view is the registration page")
    void userProfile() throws Exception {
        mockMvc.perform(get("/userProfile"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name(ViewNameConstants.USER_PROFILE));
    }
}
