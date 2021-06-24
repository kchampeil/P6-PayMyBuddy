package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.ViewNameConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String showHomePage() {
        return ViewNameConstants.HOME;
        //TODO revoir car si user connecté on revient à sa page d'accueil
    }

    @GetMapping("/underConstruction")
    public String showUnderConstructionPage() {
        return ViewNameConstants.UNDER_CONSTRUCTION;
    }

}
