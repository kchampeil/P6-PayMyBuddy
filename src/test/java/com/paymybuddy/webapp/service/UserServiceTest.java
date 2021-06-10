package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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
                "THEN an PMB Exception is thrown")
        void createUser_WithExistingEmailInRepository() {
            //GIVEN
            when(userRepositoryMock.findByEmailIgnoreCase(userDTOToCreate.getEmail()))
                    .thenReturn(Optional.of(userInDb));

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> userService.createUser(userDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.ALREADY_EXIST_USER);
            verify(userRepositoryMock, Mockito.times(1))
                    .findByEmailIgnoreCase(userDTOToCreate.getEmail());
            verify(userRepositoryMock, Mockito.times(0)).save(any(User.class));
        }


        @Test
        @DisplayName("GIVEN a new user to add with missing informations" +
                "WHEN saving this new user " +
                "THEN an PMB Exception is thrown")
        void createUser_WithMissingInformations() {
            //GIVEN
            userDTOToCreate.setLastname(null);

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> userService.createUser(userDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.MISSING_INFORMATION_NEW_USER);
            verify(userRepositoryMock, Mockito.times(0))
                    .findByEmailIgnoreCase(userDTOToCreate.getEmail());
            verify(userRepositoryMock, Mockito.times(0)).save(any(User.class));
        }


        @Test
        @DisplayName("GIVEN a new user to add with incorrect email" +
                "WHEN saving this new user " +
                "THEN an PMB Exception is thrown")
        void createUser_WithIncorrectEmail() {
            //GIVEN
            userDTOToCreate.setEmail(UserTestConstants.NEW_USER_INVALID_EMAIL);

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> userService.createUser(userDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.INVALID_EMAIL);
            verify(userRepositoryMock, Mockito.times(0))
                    .findByEmailIgnoreCase(userDTOToCreate.getEmail());
            verify(userRepositoryMock, Mockito.times(0)).save(any(User.class));
        }
    }
}