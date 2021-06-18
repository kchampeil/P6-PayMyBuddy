package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.ViewNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class BankAccountController {

    /**
     * afficher la page d'accueil compte bancaire
     */
    //TODO à revoir
    @GetMapping(value = "/bankAccount")
    public String showHomeBankAccount() {

        return ViewNameConstants.UNDER_CONSTRUCTION;

    }
}
