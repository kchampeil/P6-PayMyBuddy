package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.ViewNameConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class RelationshipController {

    /**
     * afficher la page d'accueil relation/connexion ami
     */
    //TODO à revoir
    @GetMapping(value = "/contact")
    public String showHomeRelationship() {

        return ViewNameConstants.RELATIONSHIP_HOME;

    }


    /**
     * ajouter une relation/connexion ami
     */
    //TODO à revoir
    @GetMapping(value = "/addRelationship")
    public String addRelationship() {

        return ViewNameConstants.UNDER_CONSTRUCTION;

    }
}
