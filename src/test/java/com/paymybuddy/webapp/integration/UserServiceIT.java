package com.paymybuddy.webapp.integration;

import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.IUserService;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        @Test
        @DisplayName("WHEN creating a new user with correct informations" +
                "THEN the returned value is the added user," +
                "AND the user is added in DB")
        public void createPersonTest_WithSuccess() throws Exception {
            UserDTO userDTOToCreate = new UserDTO();
            userDTOToCreate.setEmail(UserTestConstants.NEW_USER_EMAIL);
            userDTOToCreate.setFirstname(UserTestConstants.NEW_USER_FIRSTNAME);
            userDTOToCreate.setLastname(UserTestConstants.NEW_USER_LASTNAME);
            userDTOToCreate.setPassword(UserTestConstants.NEW_USER_PASSWORD);
            userDTOToCreate.setBalance(UserTestConstants.NEW_USER_BALANCE);

            Optional<UserDTO> userDTOCreated = userService.createUser(userDTOToCreate);
            Optional<User> userCreated = userRepository.findByEmailIgnoreCase(userDTOToCreate.getEmail());

            assertThat(userDTOCreated).isPresent();
            assertThat(userCreated).isPresent();
            assertEquals(userDTOToCreate.getEmail(), userCreated.get().getEmail());

            //on nettoie la DB en fin de test en supprimant la personne créée
            userRepository.deleteById(userCreated.get().getUserId());
        }


        @Test
        @DisplayName("WHEN creating a new user with an existing email in DB" +
                "THEN an PMBException is thrown AND the user is not added in DB (existing user is unchanged)")
        public void createPersonTest_AlreadyExists() {
            //initialisation du test avec une personne en base
            User existingUser = new User();
            existingUser.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
            existingUser.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
            existingUser.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
            existingUser.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
            existingUser.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
            existingUser = userRepository.save(existingUser);

            //test
            UserDTO userDTOToCreate = new UserDTO();
            userDTOToCreate.setEmail(existingUser.getEmail());
            userDTOToCreate.setFirstname(UserTestConstants.NEW_USER_FIRSTNAME);
            userDTOToCreate.setLastname(UserTestConstants.NEW_USER_LASTNAME);
            userDTOToCreate.setPassword(UserTestConstants.NEW_USER_PASSWORD);
            userDTOToCreate.setBalance(UserTestConstants.NEW_USER_BALANCE);

            assertThrows(PMBException.class, () -> userService.createUser(userDTOToCreate));

            Optional<User> userWithDefinedEmail = userRepository.findByEmailIgnoreCase(userDTOToCreate.getEmail());
            assertThat(userWithDefinedEmail).isPresent();
            assertEquals(existingUser, userWithDefinedEmail.get());

            //on nettoie la DB en fin de test en supprimant la personne créée
            userRepository.deleteById(existingUser.getUserId());
        }
    }
}
