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
    @GetMapping(value = "/profile")
    public String showHomeBankAccount() {

        return ViewNameConstants.BANK_ACCOUNT_HOME;

    }

    /**
     * ajouter un compte bancaire
     */
    //TODO à revoir
    @GetMapping(value = "/addBankAccount")
    public String addBankAccount() {

        return ViewNameConstants.BANK_ACCOUNT_ADD;

    }
}
