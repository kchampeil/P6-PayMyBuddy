package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.ViewNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class TransactionController {

    /**
     * afficher la page d'accueil transaction
     */
    //TODO à revoir
    @GetMapping(value = "/transaction")
    public String showHomeTransaction() {

        return ViewNameConstants.UNDER_CONSTRUCTION;

    }
}
