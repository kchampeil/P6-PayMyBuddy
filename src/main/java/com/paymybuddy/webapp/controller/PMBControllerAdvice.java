package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.service.PMBUserDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@Slf4j
@ControllerAdvice
public class PMBControllerAdvice {

    private final PMBUserDetailsService pmbUserDetailsService;

    @Autowired
    public PMBControllerAdvice(PMBUserDetailsService pmbUserDetailsService) {
        this.pmbUserDetailsService = pmbUserDetailsService;
    }

    @ModelAttribute
    public void addUserToModel(Principal principal, Model model) {

        User currentUser = null;
        if (principal != null) {
            currentUser = pmbUserDetailsService.getCurrentUser();
        } else {
            log.info(LogConstants.CURRENT_USER_UNKNOWN);
        }
        model.addAttribute("user", currentUser);

    }
}
