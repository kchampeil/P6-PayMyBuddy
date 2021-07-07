package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.service.contract.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@Controller
public class UserController {

    private final IUserService userService;

    @Autowired
    public UserController(IUserService userService) {
        this.userService = userService;
    }


    /**
     * afficher le formulaire d'inscription
     */
    @GetMapping(value = "/registerUser")
    public String showRegistrationForm(Model model) {
        log.info(LogConstants.USER_REGISTRATION_SHOW_PAGE_RECEIVED);

        model.addAttribute("userToRegister", new UserDTO());
        return ViewNameConstants.USER_REGISTRATION;
    }


    /**
     * inscrire un nouvel utilisateur
     *
     * @param userDTOToRegister informations sur le nouvel utilisateur à créer
     */
    @PostMapping(value = "/registerUser")
    public String saveUser(@ModelAttribute("userToRegister") @Valid UserDTO userDTOToRegister,
                           BindingResult bindingResult, Model model) {

        log.info(LogConstants.USER_REGISTRATION_REQUEST_RECEIVED + userDTOToRegister.getEmail());

        if (bindingResult.hasErrors()) {
            log.error(LogConstants.USER_REGISTRATION_REQUEST_NOT_VALID + "\n");
            return ViewNameConstants.USER_REGISTRATION;
        }

        try {
            Optional<UserDTO> userDTORegistered = userService.createUser(userDTOToRegister);

            if (userDTORegistered.isPresent()) {
                log.info(LogConstants.USER_REGISTRATION_REQUEST_OK + userDTORegistered.get().getUserId() + "\n");
                model.addAttribute("user", userDTORegistered.get());

                return ViewNameConstants.USER_REGISTRATION_SUCCESSFUL;
                /*TODO V2 pour l'instant affiche une page de confirmation d'enregistrement,
                   l'autologin sera mis en place dans la prochaine version
                 */

            } else {
                log.error(LogConstants.USER_REGISTRATION_REQUEST_KO + "\n");
                return ViewNameConstants.USER_REGISTRATION;
            }

        } catch (PMBException pmbException) {
            log.error(LogConstants.USER_REGISTRATION_REQUEST_KO + ": " + pmbException.getMessage() + " \n");

            if (pmbException.getMessage().equals(PMBExceptionConstants.ALREADY_EXIST_USER)) {
                bindingResult.rejectValue("email", "registrationForm.userDTO.email.alreadyExists");
            }
            return ViewNameConstants.USER_REGISTRATION;
        }
    }


    /**
     * connecter l'utilisateur
     */
    @GetMapping(value = "/login")
    public String loginUser() {

        return ViewNameConstants.USER_LOGIN;

    }


    /**
     * réinitialiser le mot de passe utilisateur
     * TODO V2 : cette fonctionnalité sera implémentée dans une prochaine version de PMB
     */
    @GetMapping(value = "/resetPassword")
    public String resetPassword() {

        return ViewNameConstants.UNDER_CONSTRUCTION;

    }
}
