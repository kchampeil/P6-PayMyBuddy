package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController {

    /**
     * affiche la page d'accueil de l'application si aucun utilisateur connecté
     * ou la page d'accueil utilisateur si un utilisateur est connecté
     */
    @GetMapping("/")
    public String showHomePage(Model model) {

        log.info(LogConstants.HOME_REQUEST_RECEIVED);

        if (model.getAttribute("user") != null) {
            return ViewNameConstants.USER_HOME;
        }

        return ViewNameConstants.HOME;
    }

    @GetMapping("/underConstruction")
    public String showUnderConstructionPage() {
        return ViewNameConstants.UNDER_CONSTRUCTION;
    }

}
