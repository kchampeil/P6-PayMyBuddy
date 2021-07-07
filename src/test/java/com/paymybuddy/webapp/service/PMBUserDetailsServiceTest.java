package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.service.contract.IUserService;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class PMBUserDetailsServiceTest {

    @MockBean
    IUserService userServiceMock;

    @Autowired
    private PMBUserDetailsService pmbUserDetailsService;


    @Nested
    @DisplayName("loadUserByUsername tests")
    class LoadUserByUsernameTest {
        @Test
        @DisplayName("GIVEN a user in DB for an email address " +
                "WHEN getting this user information " +
                "THEN the returned value is the user information")
        void loadUserByUsernameTest_WithSuccess() {

            //GIVEN
            UserDTO userInDb;
            userInDb = new UserDTO();
            userInDb.setUserId(UserTestConstants.EXISTING_USER_ID);
            userInDb.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
            userInDb.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
            userInDb.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
            userInDb.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
            userInDb.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);

            when(userServiceMock.getUserDTOByEmail(UserTestConstants.EXISTING_USER_EMAIL))
                    .thenReturn(userInDb);

            //WHEN
            UserDetails userDetails = pmbUserDetailsService.loadUserByUsername(UserTestConstants.EXISTING_USER_EMAIL);

            //THEN
            assertNotNull(userDetails);
            assertEquals(userInDb.getEmail(), userDetails.getUsername());
            assertEquals(userInDb.getPassword(), userDetails.getPassword());
            assertEquals(new HashSet<>(), userDetails.getAuthorities());
            assertTrue(userDetails.isAccountNonExpired());
            assertTrue(userDetails.isAccountNonLocked());
            assertTrue(userDetails.isEnabled());
            assertTrue(userDetails.isCredentialsNonExpired());

            verify(userServiceMock, Mockito.times(1))
                    .getUserDTOByEmail(UserTestConstants.EXISTING_USER_EMAIL);
        }


        @Test
        @DisplayName("GIVEN no user in DB for a given email address " +
                "WHEN getting this user information " +
                "THEN a UsernameNotFoundException exception is thrown")
        void loadUserByUsernameTest_WithUnknownUser() {

            //GIVEN
            when(userServiceMock.getUserDTOByEmail(UserTestConstants.UNKNOWN_USER_EMAIL))
                    .thenThrow(new UsernameNotFoundException(PMBExceptionConstants.DOES_NOT_EXISTS_USER));

            //THEN
            Exception exception = assertThrows(UsernameNotFoundException.class,
                    () -> pmbUserDetailsService.loadUserByUsername(UserTestConstants.UNKNOWN_USER_EMAIL));
            assertEquals(PMBExceptionConstants.DOES_NOT_EXISTS_USER, exception.getMessage());

            verify(userServiceMock, Mockito.times(1))
                    .getUserDTOByEmail(UserTestConstants.UNKNOWN_USER_EMAIL);
        }


        @Test
        @DisplayName("GIVEN a null email " +
                "WHEN getting this user information " +
                "THEN an UsernameNotFoundException is thrown")
        void loadUserByUsernameTest_WithMissingInformations() {
            //GIVEN
            when(userServiceMock.getUserDTOByEmail(null))
                    .thenThrow(new UsernameNotFoundException(PMBExceptionConstants.MISSING_INFORMATION_GETTING_USER));

            //THEN
            Exception exception = assertThrows(UsernameNotFoundException.class,
                    () -> pmbUserDetailsService.loadUserByUsername(null));
            assertEquals(PMBExceptionConstants.MISSING_INFORMATION_GETTING_USER, exception.getMessage());

            verify(userServiceMock, Mockito.times(0))
                    .getUserDTOByEmail(anyString());
        }
    }
}