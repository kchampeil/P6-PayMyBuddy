package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.RelationshipDTO;
import com.paymybuddy.webapp.model.Relationship;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.RelationshipRepository;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.testconstants.RelationshipTestConstants;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class RelationshipServiceTest {

    @MockBean
    private RelationshipRepository relationshipRepositoryMock;

    @MockBean
    private UserRepository userRepositoryMock;

    @Autowired
    private IRelationshipService relationshipService;

    @BeforeEach
    void setUp() {
    }


    @Nested
    @DisplayName("createRelationship tests")
    class CreateRelationshipTest {

        private RelationshipDTO relationshipDTOToCreate;

        private Relationship relationshipInDb;
        private User userInDb;
        private User friendInDb;

        @BeforeEach
        private void setUpPerTest() {
            relationshipDTOToCreate = new RelationshipDTO();
            relationshipDTOToCreate.setUserId(UserTestConstants.EXISTING_USER_ID);
            relationshipDTOToCreate.setFriendId(UserTestConstants.EXISTING_USER_AS_FRIEND_ID);

            userInDb = new User();
            userInDb.setUserId(UserTestConstants.EXISTING_USER_ID);
            userInDb.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
            userInDb.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
            userInDb.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
            userInDb.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
            userInDb.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);

            friendInDb = new User();
            friendInDb.setUserId(UserTestConstants.EXISTING_USER_AS_FRIEND_ID);
            friendInDb.setEmail(UserTestConstants.EXISTING_USER_AS_FRIEND_EMAIL);
            friendInDb.setFirstname(UserTestConstants.EXISTING_USER_AS_FRIEND_FIRSTNAME);
            friendInDb.setLastname(UserTestConstants.EXISTING_USER_AS_FRIEND_LASTNAME);
            friendInDb.setPassword(UserTestConstants.EXISTING_USER_AS_FRIEND_PASSWORD);
            friendInDb.setBalance(UserTestConstants.EXISTING_USER_AS_FRIEND_BALANCE);

            relationshipInDb = new Relationship();
            relationshipInDb.setRelationshipId(RelationshipTestConstants.EXISTING_RELATIONSHIP_ID);
            relationshipInDb.setUser(userInDb);
            relationshipInDb.setFriend(friendInDb);
        }

        @Test
        @DisplayName("GIVEN a new relationship to add " +
                "WHEN saving this new relationship " +
                "THEN the returned value is the added relationship")
        void createRelationship_WithSuccess() throws PMBException {
            //GIVEN
            when(userRepositoryMock.findById(relationshipDTOToCreate.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(userRepositoryMock.findById(relationshipDTOToCreate.getFriendId()))
                    .thenReturn(Optional.ofNullable(friendInDb));
            when(relationshipRepositoryMock
                    .findByUserAndFriend(userInDb, friendInDb))
                    .thenReturn(Optional.empty());

            when(relationshipRepositoryMock.save(any(Relationship.class))).thenReturn(relationshipInDb);

            //WHEN
            Optional<RelationshipDTO> createdRelationshipDTO = relationshipService.createRelationship(relationshipDTOToCreate);

            //THEN
            assertTrue(createdRelationshipDTO.isPresent());
            assertNotNull(createdRelationshipDTO.get().getRelationshipId());
            assertEquals(relationshipDTOToCreate.getUserId(), createdRelationshipDTO.get().getUserId());
            assertEquals(relationshipDTOToCreate.getUserId(), createdRelationshipDTO.get().getUserId());
            assertEquals(relationshipDTOToCreate.getFriendId(), createdRelationshipDTO.get().getFriendId());

            verify(userRepositoryMock, Mockito.times(1))
                    .findById(relationshipDTOToCreate.getUserId());
            verify(userRepositoryMock, Mockito.times(1))
                    .findById(relationshipDTOToCreate.getFriendId());
            verify(relationshipRepositoryMock, Mockito.times(1))
                    .findByUserAndFriend(userInDb, friendInDb);
            verify(relationshipRepositoryMock, Mockito.times(1))
                    .save(any(Relationship.class));
        }


        @Test
        @DisplayName("GIVEN a new relationship to add with the same relationship already existing for this user " +
                "WHEN saving this new relationship " +
                "THEN an PMB Exception is thrown")
        void createRelationship_WithExistingRelationshipInRepository() {
            //GIVEN
            when(userRepositoryMock.findById(relationshipDTOToCreate.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(userRepositoryMock.findById(relationshipDTOToCreate.getFriendId()))
                    .thenReturn(Optional.ofNullable(friendInDb));
            when(relationshipRepositoryMock
                    .findByUserAndFriend(userInDb, friendInDb))
                    .thenReturn(Optional.ofNullable(relationshipInDb));

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> relationshipService.createRelationship(relationshipDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.ALREADY_EXIST_RELATIONSHIP);

            verify(userRepositoryMock, Mockito.times(1))
                    .findById(relationshipDTOToCreate.getUserId());
            verify(userRepositoryMock, Mockito.times(1))
                    .findById(relationshipDTOToCreate.getFriendId());
            verify(relationshipRepositoryMock, Mockito.times(1))
                    .findByUserAndFriend(userInDb, friendInDb);
            verify(relationshipRepositoryMock, Mockito.times(0))
                    .save(any(Relationship.class));
        }


        @Test
        @DisplayName("GIVEN a new relationship to add with missing informations " +
                "WHEN saving this new relationship " +
                "THEN an PMB Exception is thrown")
        void createRelationship_WithMissingInformations() {
            //GIVEN
            relationshipDTOToCreate.setUserId(null);

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> relationshipService.createRelationship(relationshipDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.MISSING_INFORMATION_NEW_RELATIONSHIP);

            verify(userRepositoryMock, Mockito.times(0))
                    .findById(relationshipDTOToCreate.getUserId());
            verify(userRepositoryMock, Mockito.times(0))
                    .findById(relationshipDTOToCreate.getFriendId());
            verify(relationshipRepositoryMock, Mockito.times(0))
                    .findByUserAndFriend(userInDb, friendInDb);
            verify(relationshipRepositoryMock, Mockito.times(0))
                    .save(any(Relationship.class));
        }


        @Test
        @DisplayName("GIVEN a new relationship to add with an unknown friend " +
                "WHEN saving this new relationship " +
                "THEN an PMB Exception is thrown")
        void createRelationship_WithUnknownFriend() {
            //GIVEN
            relationshipDTOToCreate.setFriendId(UserTestConstants.UNKNOWN_USER_ID);
            when(userRepositoryMock.findById(relationshipDTOToCreate.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(userRepositoryMock.findById(relationshipDTOToCreate.getFriendId()))
                    .thenReturn(Optional.empty());

            //THEN
            Exception exception = assertThrows(PMBException.class, () -> relationshipService.createRelationship(relationshipDTOToCreate));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.DOES_NOT_EXISTS_USER);

            verify(userRepositoryMock, Mockito.times(1))
                    .findById(relationshipDTOToCreate.getUserId());
            verify(userRepositoryMock, Mockito.times(1))
                    .findById(relationshipDTOToCreate.getFriendId());
            verify(relationshipRepositoryMock, Mockito.times(0))
                    .findByUserAndFriend(userInDb, friendInDb);
            verify(relationshipRepositoryMock, Mockito.times(0))
                    .save(any(Relationship.class));
        }
    }


    @Nested
    @DisplayName("getAllRelationshipsForUser tests")
    class GetAllRelationshipsForUserTest {

        private Relationship relationshipInDb;
        private User userInDb;
        private User friendInDb;

        @BeforeEach
        private void setUpPerTest() {
            relationshipInDb = new Relationship();
            relationshipInDb.setRelationshipId(RelationshipTestConstants.EXISTING_RELATIONSHIP_ID);

            userInDb = new User();
            userInDb.setUserId(UserTestConstants.EXISTING_USER_ID);
            userInDb.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
            userInDb.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
            userInDb.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
            userInDb.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
            userInDb.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
            relationshipInDb.setUser(userInDb);

            friendInDb = new User();
            friendInDb.setUserId(UserTestConstants.EXISTING_USER_AS_FRIEND_ID);
            friendInDb.setEmail(UserTestConstants.EXISTING_USER_AS_FRIEND_EMAIL);
            friendInDb.setFirstname(UserTestConstants.EXISTING_USER_AS_FRIEND_FIRSTNAME);
            friendInDb.setLastname(UserTestConstants.EXISTING_USER_AS_FRIEND_LASTNAME);
            friendInDb.setPassword(UserTestConstants.EXISTING_USER_AS_FRIEND_PASSWORD);
            friendInDb.setBalance(UserTestConstants.EXISTING_USER_AS_FRIEND_BALANCE);
            relationshipInDb.setFriend(friendInDb);
        }


        @Test
        @DisplayName("GIVEN relationships in DB for an existing user " +
                "WHEN getting all the relationships for this user " +
                "THEN the returned value is the list of relationships")
        void getAllRelationshipsForUser_WithDataInDB() throws PMBException {
            //GIVEN
            List<Relationship> relationshipList = new ArrayList<>();
            relationshipList.add(relationshipInDb);

            when(userRepositoryMock.findById(userInDb.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(relationshipRepositoryMock.findAllByUser_UserId(userInDb.getUserId()))
                    .thenReturn(relationshipList);

            //THEN
            List<RelationshipDTO> relationshipDTOList =
                    relationshipService.getAllRelationshipsForUser(userInDb.getUserId());
            assertEquals(1, relationshipDTOList.size());
            assertEquals(relationshipInDb.getRelationshipId(), relationshipDTOList.get(0).getRelationshipId());
            assertEquals(friendInDb.getUserId(), relationshipDTOList.get(0).getFriendId());

            verify(userRepositoryMock, Mockito.times(1)).findById(userInDb.getUserId());
            verify(relationshipRepositoryMock, Mockito.times(1))
                    .findAllByUser_UserId(userInDb.getUserId());
        }


        @Test
        @DisplayName("GIVEN no relationships in DB for an existing user " +
                "WHEN getting all the relationships for this user " +
                "THEN the returned value is an empty list of relationships")
        void getAllRelationshipsForUser_WithNoDataInDB() throws PMBException {
            //GIVEN
            List<Relationship> relationshipList = new ArrayList<>();

            when(userRepositoryMock.findById(userInDb.getUserId()))
                    .thenReturn(Optional.ofNullable(userInDb));
            when(relationshipRepositoryMock.findAllByUser_UserId(userInDb.getUserId()))
                    .thenReturn(relationshipList);

            //THEN
            List<RelationshipDTO> relationshipDTOList =
                    relationshipService.getAllRelationshipsForUser(userInDb.getUserId());
            assertThat(relationshipDTOList).isEmpty();

            verify(userRepositoryMock, Mockito.times(1)).findById(userInDb.getUserId());
            verify(relationshipRepositoryMock, Mockito.times(1))
                    .findAllByUser_UserId(userInDb.getUserId());
        }


        @Test
        @DisplayName("GIVEN an unknown user " +
                "WHEN getting all the relationships for this user " +
                "THEN an PMB Exception is thrown")
        void getAllRelationshipsForUser_WithUnknownUser() {
            //GIVEN
            when(userRepositoryMock.findById(UserTestConstants.UNKNOWN_USER_ID))
                    .thenReturn(Optional.empty());

            //THEN
            Exception exception = assertThrows(PMBException.class,
                    () -> relationshipService.getAllRelationshipsForUser(UserTestConstants.UNKNOWN_USER_ID));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.DOES_NOT_EXISTS_USER);

            verify(userRepositoryMock, Mockito.times(1))
                    .findById(UserTestConstants.UNKNOWN_USER_ID);
            verify(relationshipRepositoryMock, Mockito.times(0))
                    .findAllByUser_UserId(UserTestConstants.UNKNOWN_USER_ID);
        }


        @Test
        @DisplayName("GIVEN an null userId " +
                "WHEN getting all the relationships for this user " +
                "THEN an PMB Exception is thrown")
        void getAllRelationshipsForUser_WithNullUserId() {
            //THEN
            Exception exception = assertThrows(PMBException.class,
                    () -> relationshipService.getAllRelationshipsForUser(null));
            assertThat(exception.getMessage()).contains(PMBExceptionConstants.MISSING_INFORMATION_LIST_RELATIONSHIP);

            verify(userRepositoryMock, Mockito.times(0))
                    .findById(anyLong());
            verify(relationshipRepositoryMock, Mockito.times(0))
                    .findAllByUser_UserId(anyLong());
        }
    }
}
