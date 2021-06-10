package com.paymybuddy.webapp.integration;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.RelationshipDTO;
import com.paymybuddy.webapp.model.Relationship;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.RelationshipRepository;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.IRelationshipService;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ActiveProfiles("test")
@SpringBootTest
public class RelationshipIT {
    @Autowired
    IRelationshipService relationshipService;

    @Autowired
    RelationshipRepository relationshipRepository;

    @Autowired
    UserRepository userRepository;

    @Nested
    @DisplayName("createRelationship IT")
    class CreateRelationshipIT {

        private User existingUser;
        private User existingFriend;
        private RelationshipDTO relationshipDTOToCreate;


        @BeforeEach
        private void setUpPerTest() {
            //initialisation avec un user en base
            existingUser = new User();
            existingUser.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
            existingUser.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
            existingUser.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
            existingUser.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
            existingUser.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
            existingUser = userRepository.save(existingUser);

            //initialisation avec un user ami en base
            existingFriend = new User();
            existingFriend.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
            existingFriend.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
            existingFriend.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
            existingFriend.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
            existingFriend.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
            existingFriend = userRepository.save(existingFriend);

            //initialisation de l objet bankAccountDTOToCreate
            relationshipDTOToCreate = new RelationshipDTO();
            relationshipDTOToCreate.setUserId(existingUser.getUserId());
            relationshipDTOToCreate.setFriendId(existingFriend.getUserId());
        }

        @AfterEach
        private void tearDownPerTest() {
            //nettoyage la DB en fin de test en supprimant le user et l'ami créés à l initialisation
            //TOASK le premier user créé n est pas supprimé
            // comme si la base ne pouvait pas être vide une fois alimentée(même avec un deleteAll ça ne la supprime pas)
            // userRepository.deleteAll();
            userRepository.deleteById(existingUser.getUserId());
            userRepository.deleteById(existingFriend.getUserId());
        }


        @Test
        @DisplayName("WHEN creating a new relationship with correct informations " +
                "for an existing user & an existing friend without any existing relationship user -> friend" +
                "THEN the returned value is the added relationship, " +
                "AND the relationship is added in DB")
        public void createRelationshipIT_WithSuccess() throws Exception {

            Optional<RelationshipDTO> relationshipDTOCreated = relationshipService.createRelationship(relationshipDTOToCreate);
            Optional<Relationship> relationshipCreated = relationshipRepository
                    .findByUserAndFriend(existingUser, existingFriend);

            assertThat(relationshipDTOCreated).isPresent();
            assertThat(relationshipCreated).isPresent();
            assertEquals(relationshipDTOToCreate.getFriendId(), relationshipCreated.get().getFriend().getUserId());

            //nettoyage de la DB en fin de test en supprimant la relation user/friend créée par le test
            relationshipRepository.deleteById(relationshipCreated.get().getRelationshipId());
        }


        @Test
        @DisplayName("WHEN creating a new relationship with an unknown friend " +
                "THEN an PMBException is thrown AND the relationship is not added in DB")
        public void createRelationshipIT_WithUnknownFriend() {

            relationshipDTOToCreate.setFriendId(UserTestConstants.UNKNOWN_USER_ID);

            Exception exception = assertThrows(PMBException.class, () -> relationshipService.createRelationship(relationshipDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.DOES_NOT_EXISTS_USER);

            Optional<Relationship> relationshipNotCreated = relationshipRepository
                    .findByUserAndFriend(existingUser, null);
            assertThat(relationshipNotCreated).isNotPresent();
        }


        @Test
        @DisplayName("WHEN creating a new relationship for an existing user/friend relationship " +
                "THEN an PMBException is thrown AND the relationship is not added in DB")
        @Transactional
        public void createRelationshipIT_AlreadyExists() {
            //initialisation du test avec une relation en base en base
            Relationship existingRelationship = new Relationship();
            existingRelationship.setUser(existingUser);
            existingRelationship.setFriend(existingFriend);
            existingRelationship = relationshipRepository.save(existingRelationship);

            //test
            relationshipDTOToCreate.setFriendId(existingFriend.getUserId());

            Exception exception = assertThrows(PMBException.class, () -> relationshipService.createRelationship(relationshipDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.ALREADY_EXIST_RELATIONSHIP);

            Optional<Relationship> relationshipAlreadyExisting = relationshipRepository
                    .findByUserAndFriend(existingUser, existingFriend);
            assertThat(relationshipAlreadyExisting).isNotEmpty();
            assertEquals(existingRelationship, relationshipAlreadyExisting.get());

            //nettoyage de la DB en fin de test en supprimant la relation user/friend créée à l initialisation du test
            //TOASK le premier CB créé n est pas supprimé comme si la base ne pouvait pas être vide une fois alimentée
            relationshipRepository.deleteById(existingRelationship.getRelationshipId());
        }
    }
}
