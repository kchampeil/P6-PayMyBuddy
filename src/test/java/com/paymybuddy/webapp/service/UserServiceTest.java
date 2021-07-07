package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.contract.IUserService;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class UserServiceTest {

    @MockBean
    private UserRepository userRepositoryMock;

    @Autowired
    private IUserService userService;


    @Nested
    @DisplayName("createUser tests")
    class CreateUserTest {

        private UserDTO userDTOToCreate;
        private User userInDb;

        @BeforeEach
        private void setUpPerTest() {
            userDTOToCreate = new UserDTO();
            userDTOToCreate.setEmail(UserTestConstants.NEW_USER_EMAIL);
            userDTOToCreate.setFirstname(UserTestConstants.NEW_USER_FIRSTNAME);
            userDTOToCreate.setLastname(UserTestConstants.NEW_USER_LASTNAME);
            userDTOToCreate.setPassword(UserTestConstants.NEW_USER_PASSWORD);
            userDTOToCreate.setBalance(UserTestConstants.NEW_USER_BALANCE);

            userInDb = new User();
            userInDb.setUserId(1L);
            userInDb.setEmail(userDTOToCreate.getEmail());
            userInDb.setFirstname(userDTOToCreate.getFirstname());
            userInDb.setLastname(userDTOToCreate.getLastname());
            userInDb.setPassword(userDTOToCreate.getPassword());
            userInDb.setBalance(userDTOToCreate.getBalance());
        }

        @Test
        @DisplayName("GIVEN a new user to add " +
                "WHEN saving this new user " +
                "THEN the returned value is the added user")
        void createUser_WithSuccess() throws PMBException {
            //GIVEN
            when(userRepositoryMock.findByEmailIgnoreCase(userDTOToCreate.getEmail()))
                    .thenReturn(Optional.empty());
            when(userRepositoryMock.save(any(User.class))).thenReturn(userInDb);

            //WHEN
            Optional<UserDTO> createdUserDTO = userService.createUser(userDTOToCreate);

            //THEN
            assertTrue(createdUserDTO.isPresent());
            assertNotNull(createdUserDTO.get().getUserId());
            assertEquals(userDTOToCreate.getEmail(), createdUserDTO.get().getEmail());
            assertEquals(userDTOToCreate.getFirstname(), createdUserDTO.get().getFirstname());
            assertEquals(userDTOToCreate.getLastname(), createdUserDTO.get().getLastname());

            verify(userRepositoryMock, Mockito.times(1))
                    .findByEmailIgnoreCase(userDTOToCreate.getEmail());
            verify(userRepositoryMock, Mockito.times(1))
                    .save(any(User.class));
        }


        @Test
        @DisplayName("GIVEN a new user to add with an already existing email" +
                "WHEN saving this new user " +
                "THEN a PMB Exception is thrown")
        void createUser_WithExistingEmailInRepository() {
            //GIVEN
            when(userRepositoryMock.findByEmailIgnoreCase(userDTOToCreate.getEmail()))
                    .thenReturn(Optional.of(userInDb));

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> userService.createUser(userDTOToCreate));
            assertEquals(PMBExceptionConstants.ALREADY_EXIST_USER, exception.getMessage());

            verify(userRepositoryMock, Mockito.times(1))
                    .findByEmailIgnoreCase(userDTOToCreate.getEmail());
            verify(userRepositoryMock, Mockito.times(0)).save(any(User.class));
        }


        @Test
        @DisplayName("GIVEN a new user to add with missing informations" +
                "WHEN saving this new user " +
                "THEN a PMB Exception is thrown")
        void createUser_WithMissingInformations() {
            //GIVEN
            userDTOToCreate.setLastname(null);

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> userService.createUser(userDTOToCreate));
            assertEquals(PMBExceptionConstants.MISSING_INFORMATION_NEW_USER, exception.getMessage());

            verify(userRepositoryMock, Mockito.times(0))
                    .findByEmailIgnoreCase(userDTOToCreate.getEmail());
            verify(userRepositoryMock, Mockito.times(0)).save(any(User.class));
        }


        @Test
        @DisplayName("GIVEN a new user to add with incorrect email" +
                "WHEN saving this new user " +
                "THEN a PMB Exception is thrown")
        void createUser_WithIncorrectEmail() {
            //GIVEN
            userDTOToCreate.setEmail(UserTestConstants.NEW_USER_INVALID_EMAIL);

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> userService.createUser(userDTOToCreate));
            assertEquals(PMBExceptionConstants.INVALID_USER_EMAIL, exception.getMessage());

            verify(userRepositoryMock, Mockito.times(0))
                    .findByEmailIgnoreCase(userDTOToCreate.getEmail());
            verify(userRepositoryMock, Mockito.times(0)).save(any(User.class));
        }
    }


    @Nested
    @DisplayName("getUserDTOByEmail tests")
    class GetUserDTOByEmailTest {

        @Test
        @DisplayName("GIVEN a user in DB for an email address " +
                "WHEN get this user information " +
                "THEN the returned value is the user information")
        void getUserDTOByEmail_WithSuccess() {
            //GIVEN
            User userInDb;
            userInDb = new User();
            userInDb.setUserId(UserTestConstants.EXISTING_USER_ID);
            userInDb.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
            userInDb.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
            userInDb.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
            userInDb.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
            userInDb.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);

            when(userRepositoryMock.findByEmailIgnoreCase(UserTestConstants.EXISTING_USER_EMAIL))
                    .thenReturn(Optional.of(userInDb));

            //WHEN
            UserDTO userDTO = userService.getUserDTOByEmail(UserTestConstants.EXISTING_USER_EMAIL);

            //THEN
            assertNotNull(userDTO.getUserId());
            assertEquals(userInDb.getEmail(), userDTO.getEmail());
            assertEquals(userInDb.getFirstname(), userDTO.getFirstname());
            assertEquals(userInDb.getLastname(), userDTO.getLastname());

            verify(userRepositoryMock, Mockito.times(1))
                    .findByEmailIgnoreCase(UserTestConstants.EXISTING_USER_EMAIL);
        }


        @Test
        @DisplayName("GIVEN no user in DB for an email address " +
                "WHEN get this user information " +
                "THEN UsernameNotFoundException is thrown")
        void getUserDTOByEmail_WithUnknownUser() {
            //GIVEN
            when(userRepositoryMock.findByEmailIgnoreCase(UserTestConstants.UNKNOWN_USER_EMAIL))
                    .thenReturn(Optional.ofNullable(null));

            //THEN
            Exception exception = assertThrows(UsernameNotFoundException.class,
                    () -> userService.getUserDTOByEmail(UserTestConstants.UNKNOWN_USER_EMAIL));
            assertEquals(PMBExceptionConstants.DOES_NOT_EXISTS_USER, exception.getMessage());

            verify(userRepositoryMock, Mockito.times(1))
                    .findByEmailIgnoreCase(UserTestConstants.UNKNOWN_USER_EMAIL);
        }


        @Test
        @DisplayName("GIVEN a null email " +
                "WHEN get this user information " +
                "THEN UsernameNotFoundException is thrown")
        void getUserDTOByEmail_WithMissingInformations() {
            //THEN
            Exception exception = assertThrows(UsernameNotFoundException.class,
                    () -> userService.getUserDTOByEmail(null));
            assertEquals(PMBExceptionConstants.MISSING_INFORMATION_GETTING_USER, exception.getMessage());

            verify(userRepositoryMock, Mockito.times(0))
                    .findByEmailIgnoreCase(null);
        }

    }
}
