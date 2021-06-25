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
    @GetMapping(value = "/newUser")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserDTO());
        return ViewNameConstants.USER_REGISTRATION;
    }


    /**
     * inscrire un nouvel utilisateur
     *
     * @param userDTOToRegister informations sur le nouvel utilisateur à créer
     */
    @PostMapping(value = "/registerUser")
    public String saveUser(@ModelAttribute("user") @Valid UserDTO userDTOToRegister,
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
                return ViewNameConstants.USER_PROFILE;

                //TODO return "redirect:/home" en mode connecté ? pour l'instant affiche le profil en mode read-only

            } else {
                log.error(LogConstants.USER_REGISTRATION_REQUEST_KO + "\n");
                return ViewNameConstants.USER_REGISTRATION;
            }

        } catch (PMBException pmbException) {
            log.error(LogConstants.USER_REGISTRATION_REQUEST_KO + ": " + pmbException.getMessage() + " \n");

            if (pmbException.getMessage().contains(PMBExceptionConstants.ALREADY_EXIST_USER)) {
                bindingResult.rejectValue("email", "registrationForm.userDTO.email.alreadyExists", pmbException.getMessage());
            }
            return ViewNameConstants.USER_REGISTRATION;
        }
    }


    /**
     * afficher le profil utilisateur après son enregistrement
     *
     * @param userDTO informations utilisateur
     */
    @GetMapping(value = "/userProfile")
    public String userProfile(UserDTO userDTO, Model model) {

        log.info(LogConstants.USER_PROFILE_REQUEST_RECEIVED + userDTO.getUserId());

        model.addAttribute("user", userDTO);
        return ViewNameConstants.USER_PROFILE;

    }


    /**
     * connecter nouvel utilisateur
     */
    //TODO changer le type de request en POST une fois implémenté
    @GetMapping(value = "/logUser")
    public String loginUser() {

        return ViewNameConstants.USER_LOGIN;

    }


    /**
     * déconnecter l'utilisateur et le renvoie sur la page d'accueil
     */
    //TODO à revoir
    @GetMapping(value = "/logoffUser")
    public String logoffUser() {

        return ViewNameConstants.HOME;

    }


    /**
     * afficher la page d'accueil utilisateur
     */
    //TODO à fusionner à la fin
    @GetMapping(value = "/homeUser")
    public String showHomeUser() {

        return ViewNameConstants.USER_HOME;

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
