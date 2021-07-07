package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.model.PMBUserDetails;
import com.paymybuddy.webapp.service.contract.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;

@Slf4j
@Service
public class PMBUserDetailsService implements UserDetailsService {

    private final IUserService userService;

    @Autowired
    public PMBUserDetailsService(IUserService userService) {
        this.userService = userService;
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
        UserDTO userDTO = userService.getUserDTOByEmail(username);

        return new PMBUserDetails(userDTO.getEmail(),
                userDTO.getPassword(),
                new HashSet<>(), //TODO V2 dans cette version démo les 'authorities' ne sont pas gérées
                true,
                true,
                true,
                true);
    }


    /**
     * récupère les informations du user courant
     *
     * @return les informations de l'utilisateur connecté
     */
    public UserDTO getCurrentUser() {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.getUserDTOByEmail(authentication.getName());
    }
}
