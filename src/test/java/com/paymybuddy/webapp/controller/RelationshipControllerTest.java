package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.RelationshipDTO;
import com.paymybuddy.webapp.service.PMBUserDetailsService;
import com.paymybuddy.webapp.service.contract.IRelationshipService;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(controllers = RelationshipController.class)
class RelationshipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IRelationshipService relationshipServiceMock;

    @MockBean
    private PMBUserDetailsService pmbUserDetailsServiceMock;

    @MockBean
    private PasswordEncoder passwordEncoderMock;


    @Nested
    @DisplayName("showHomeRelationship tests")
    class ShowHomeRelationshipTest {

        @WithMockUser
        @Test
        @DisplayName("WHEN asking for the contact page while logged in " +
                " THEN return status is ok and the expected view is the contact page")
        void showHomeRelationshipTest_LoggedIn() throws Exception {
            mockMvc.perform(get("/contact"))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("relationshipDTO"))
                    .andExpect(model().attributeExists("relationshipDTOList"))
                    .andExpect(view().name(ViewNameConstants.RELATIONSHIP_HOME));
        }


        @Test
        @DisplayName("WHEN asking for the contact page while not logged in " +
                " THEN return status is 302 and the expected view is the login page")
        void showHomeRelationshipTest_NotLoggedIn() throws Exception {
            mockMvc.perform(get("/contact"))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrlPattern("**/" + ViewNameConstants.USER_LOGIN));
        }
    }

    @Nested
    @DisplayName("addRelationship tests")
    class AddRelationshipTest {

        @WithMockUser
        @Test
        @DisplayName("GIVEN a new relationship to register " +
                "WHEN processing a POST /contact request for this relationship " +
                "THEN return status is ok " +
                "AND the expected view is the contact page with relationship list updated")
        void addRelationshipTest_WithSuccess() throws Exception {
            //GIVEN
            RelationshipDTO relationshipDTOAdded = new RelationshipDTO();
            relationshipDTOAdded.setUserId(UserTestConstants.EXISTING_USER_ID);
            relationshipDTOAdded.setFriendEmail(UserTestConstants.EXISTING_USER_AS_FRIEND_EMAIL);

            when(relationshipServiceMock.createRelationship(any(RelationshipDTO.class)))
                    .thenReturn(Optional.of(relationshipDTOAdded));

            //THEN
            mockMvc.perform(post("/contact")
                    .param("friendEmail", relationshipDTOAdded.getFriendEmail())
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("relationshipDTO"))
                    .andExpect(model().attributeExists("relationshipDTOList"))
                    .andExpect(view().name(ViewNameConstants.RELATIONSHIP_HOME));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a new relationship to register with missing email " +
                "WHEN processing a POST /contact request for this relationship " +
                "THEN the returned code is ok " +
                "AND the expected view is the contact page filled with entered friend email")
        void addRelationshipTest_WithMissingInformation() throws Exception {
            //GIVEN
            when(relationshipServiceMock.createRelationship(any(RelationshipDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_RELATIONSHIP));

            //THEN
            mockMvc.perform(post("/contact")
                    .param("friendEmail", "")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("relationshipDTO"))
                    .andExpect(model().hasErrors())
                    .andExpect(model().attributeHasFieldErrors("relationshipDTO", "friendEmail"))
                    .andExpect(view().name(ViewNameConstants.RELATIONSHIP_HOME));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a friend email already present in relationship list " +
                "WHEN processing a POST /contact request for this friend email " +
                "THEN the returned code is ok " +
                "AND the expected view is the contact page filled with friend email " +
                "AND an 'already exists' error is shown")
        void addRelationshipTest_WithAlreadyExistingRelationship() throws Exception {
            //GIVEN
            when(relationshipServiceMock.createRelationship(any(RelationshipDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.ALREADY_EXIST_RELATIONSHIP));

            //THEN
            mockMvc.perform(post("/contact")
                    .param("friendEmail", UserTestConstants.EXISTING_USER_AS_FRIEND_EMAIL)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("relationshipDTO"))
                    .andExpect(model().attributeHasFieldErrorCode(
                            "relationshipDTO",
                            "friendEmail",
                            "contact.RelationshipDTO.friend.alreadyExists"))
                    .andExpect(view().name(ViewNameConstants.RELATIONSHIP_HOME));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a friend email not present in PMB " +
                "WHEN processing a POST /contact request for this friend email " +
                "THEN the returned code is ok " +
                "AND the expected view is the contact page filled with friend email " +
                "AND an 'does not exist' error is shown")
        void addRelationshipTest_WithUnknownFriendEmail() throws Exception {
            //GIVEN
            when(relationshipServiceMock.createRelationship(any(RelationshipDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_USER));

            //THEN
            mockMvc.perform(post("/contact")
                    .param("friendEmail", UserTestConstants.UNKNOWN_USER_EMAIL)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("relationshipDTO"))
                    .andExpect(model().attributeHasFieldErrorCode(
                            "relationshipDTO",
                            "friendEmail",
                            "contact.RelationshipDTO.email.doesNotExist"))
                    .andExpect(view().name(ViewNameConstants.RELATIONSHIP_HOME));
        }


        @WithMockUser
        @Test
        @DisplayName("GIVEN a friend email equals to current user email " +
                "WHEN processing a POST /contact request for this friend email " +
                "THEN the returned code is ok " +
                "AND the expected view is the contact page filled with friend email " +
                "AND an 'invalid email' error is shown")
        void addRelationshipTest_WithInvalidEmail() throws Exception {
            //GIVEN
            when(relationshipServiceMock.createRelationship(any(RelationshipDTO.class)))
                    .thenThrow(new PMBException(PMBExceptionConstants.INVALID_FRIEND_EMAIL));

            //THEN
            mockMvc.perform(post("/contact")
                    .param("friendEmail", UserTestConstants.EXISTING_USER_EMAIL)
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(model().attributeExists("relationshipDTO"))
                    .andExpect(model().attributeHasFieldErrorCode(
                            "relationshipDTO",
                            "friendEmail",
                            "contact.RelationshipDTO.email.invalid"))
                    .andExpect(view().name(ViewNameConstants.RELATIONSHIP_HOME));
        }
    }
}