package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.model.PMBUserDetails;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Slf4j
@Service
public class PMBUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public PMBUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    /**
     * récupère les données utilisateur sur la base de son username (email dans PMB)
     *
     * @param username email de l'utilisateur servant d'identifiant à l'application
     * @return UserDetails alimentés avec les informations utilisateur
     * @throws UsernameNotFoundException si l'utilisateur n'a pas été trouvé
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info(LogConstants.USER_LOGIN_REQUEST_RECEIVED + username);

        if (username == null || username.isEmpty()) {
            log.error(LogConstants.GET_USER_INFO_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_GETTING_USER);
            throw new UsernameNotFoundException(PMBExceptionConstants.MISSING_INFORMATION_GETTING_USER);
        }

        User user = userRepository
                .findByEmailIgnoreCase(username)
                .orElseThrow(() -> {
                    log.error(PMBExceptionConstants.DOES_NOT_EXISTS_USER + " for: " + username);
                    return new UsernameNotFoundException(PMBExceptionConstants.DOES_NOT_EXISTS_USER);
                });

        log.info(LogConstants.GET_USER_INFO_OK + username + "\n");

        return new PMBUserDetails(user.getEmail(),
                user.getPassword(),
                new HashSet<>(), //TODO V2 dans cette version démo les 'authorities' ne sont pas gérées
                true,
                true,
                true,
                true);
    }
}
