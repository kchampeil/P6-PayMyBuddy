package com.paymybuddy.webapp.integration;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest
public class UserServiceIT {

    @Autowired
    IUserService userService;

    @Autowired
    UserRepository userRepository;

    @Nested
    @DisplayName("createUser IT")
    class CreateUserIT {

        private UserDTO userDTOToCreate;

        @BeforeEach
        private void setUpPerTest() {
            userDTOToCreate = new UserDTO();
            userDTOToCreate.setEmail(UserTestConstants.NEW_USER_EMAIL);
            userDTOToCreate.setFirstname(UserTestConstants.NEW_USER_FIRSTNAME);
            userDTOToCreate.setLastname(UserTestConstants.NEW_USER_LASTNAME);
            userDTOToCreate.setPassword(UserTestConstants.NEW_USER_PASSWORD);
            userDTOToCreate.setBalance(UserTestConstants.NEW_USER_BALANCE);
        }


        @Test
        @DisplayName("WHEN creating a new user with correct informations" +
                "THEN the returned value is the added user," +
                "AND the user is added in DB")
        public void createUserIT_WithSuccess() throws Exception {

            Optional<UserDTO> userDTOCreated = userService.createUser(userDTOToCreate);
            Optional<User> userCreated = userRepository.findByEmailIgnoreCase(userDTOToCreate.getEmail());

            assertThat(userDTOCreated).isPresent();
            assertNotNull(userDTOCreated.get().getUserId());

            assertThat(userCreated).isPresent();
            assertEquals(userDTOToCreate.getEmail(), userCreated.get().getEmail());

            //nettoyage de la DB en fin de test en supprimant l utilisateur créé
            userRepository.deleteById(userCreated.get().getUserId());
        }


        @Test
        @DisplayName("WHEN creating a new user with an existing email in DB " +
                "THEN an PMBException is thrown AND the user is not added in DB (existing user is unchanged)")
        public void createUserIT_AlreadyExists() {
            //initialisation du test avec un user en base
            User existingUser = new User();
            existingUser.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
            existingUser.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
            existingUser.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
            existingUser.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
            existingUser.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
            existingUser = userRepository.save(existingUser);

            //test
            userDTOToCreate.setEmail(existingUser.getEmail());

            Exception exception = assertThrows(PMBException.class, () -> userService.createUser(userDTOToCreate));
            assertEquals(PMBExceptionConstants.ALREADY_EXIST_USER, exception.getMessage());

            Optional<User> userWithDefinedEmail = userRepository.findByEmailIgnoreCase(userDTOToCreate.getEmail());
            assertThat(userWithDefinedEmail).isPresent();
            assertEquals(existingUser, userWithDefinedEmail.get());

            //nettoyage la DB en fin de test en supprimant l utilisateur créé
            userRepository.deleteById(existingUser.getUserId());
        }
    }


    @Test
    @DisplayName("WHEN getting the user information for an existing user " +
            "THEN the user information in DB is returned")
    public void getUserByEmailIT_WithData() throws PMBException {

        //initialisation du test avec un utilisateur en base
        User existingUser = new User();
        existingUser.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
        existingUser.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
        existingUser.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
        existingUser.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
        existingUser.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
        existingUser = userRepository.save(existingUser);

        //test
        Optional<UserDTO> userDTO = userService.getUserDTOByEmail(existingUser.getEmail());

        assertThat(userDTO).isNotEmpty();
        assertEquals(existingUser.getUserId(), userDTO.get().getUserId());

        //nettoyage de la DB en fin de test en supprimant l'utilisateur' créé par le test
        userRepository.deleteById(existingUser.getUserId());
    }
}
