package com.paymybuddy.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

    @RequestMapping("/")
    public String home(Model model)
    {
        return "home";
        //TODO revoir car si user connecté on revient à sa page d'accueil
    }

}
