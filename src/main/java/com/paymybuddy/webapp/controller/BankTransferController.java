package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.ViewNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class BankTransferController {

    /**
     * afficher la page d'accueil transfert bancaire
     */
    //TODO à revoir
    @GetMapping(value = "/bankTransfer")
    public String showHomeBankTransfer() {

        return ViewNameConstants.UNDER_CONSTRUCTION;

    }
}
