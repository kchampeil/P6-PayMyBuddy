package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.service.contract.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@Slf4j
@ControllerAdvice
public class PMBControllerAdvice {

    private final IUserService userService;

    @Autowired
    public PMBControllerAdvice(IUserService userService) {
        this.userService = userService;
    }

    @ModelAttribute
    public void addUserToModel(Principal principal, Model model) {

        UserDTO currentUser = null;

        if (principal != null) {
            currentUser = userService.getUserDTOByEmail(principal.getName());

        } else {
            log.info(LogConstants.CURRENT_USER_UNKNOWN);
        }
        model.addAttribute("user", currentUser);

    }
}
