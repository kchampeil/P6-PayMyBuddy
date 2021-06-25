package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.RelationshipDTO;
import com.paymybuddy.webapp.service.contract.IRelationshipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Slf4j
@Controller
public class RelationshipController {

    private final IRelationshipService relationshipService;

    @Autowired
    public RelationshipController(IRelationshipService relationshipService) {
        this.relationshipService = relationshipService;
    }


    /**
     * afficher la page d'accueil relation/connexion ami
     */
    //TODO à revoir
    @GetMapping(value = "/contact")
    public String showHomeRelationship(Model model) throws PMBException {

        model.addAttribute("relationshipDTO", new RelationshipDTO());

        List<RelationshipDTO> relationshipDTOList = relationshipService.getAllRelationshipsForUser(20L);
        //TODO-debouchonnage passer le user ID ensuite
        model.addAttribute("relationshipDTOList", relationshipDTOList);

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
