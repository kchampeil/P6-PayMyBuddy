package com.paymybuddy.webapp.integration;

import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.testconstants.UserTestConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest
public class PMBUSerDetailsServiceIT {

    @Autowired
    UserRepository userRepository;

    @Autowired
    UserDetailsService pmbUserDetailsService;

    @Test
    @DisplayName("WHEN loading the user information for an existing user " +
            "THEN the user information in DB is returned")
    public void loadUserByUsernameIT_WithData() {

        //initialisation du test avec un utilisateur en base
        User existingUser = new User();
        existingUser.setEmail(UserTestConstants.EXISTING_USER_EMAIL);
        existingUser.setFirstname(UserTestConstants.EXISTING_USER_FIRSTNAME);
        existingUser.setLastname(UserTestConstants.EXISTING_USER_LASTNAME);
        existingUser.setBalance(UserTestConstants.EXISTING_USER_WITH_HIGH_BALANCE);
        existingUser.setPassword(UserTestConstants.EXISTING_USER_PASSWORD);
        existingUser = userRepository.save(existingUser);

        //test
        UserDetails pmbUserDetails = pmbUserDetailsService.loadUserByUsername(existingUser.getEmail());

        assertNotNull(pmbUserDetails);
        assertEquals(existingUser.getEmail(), pmbUserDetails.getUsername());
        assertEquals(existingUser.getPassword(), pmbUserDetails.getPassword());
        assertEquals(new HashSet<>(), pmbUserDetails.getAuthorities());
        assertTrue(pmbUserDetails.isAccountNonExpired());
        assertTrue(pmbUserDetails.isAccountNonLocked());
        assertTrue(pmbUserDetails.isEnabled());
        assertTrue(pmbUserDetails.isCredentialsNonExpired());


        //nettoyage de la DB en fin de test en supprimant l'utilisateur' créé par le test
        userRepository.deleteById(existingUser.getUserId());
    }
}
