package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.RelationshipDTO;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.service.contract.IRelationshipService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

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
    @GetMapping(value = "/contact")
    public String showHomeRelationship(Model model) throws PMBException {

        log.info(LogConstants.GET_RELATIONSHIP_REQUEST_RECEIVED);

        model.addAttribute("relationshipDTO", new RelationshipDTO());

        //récupération de la liste des relations associées à l'utilisateur connecté
        loadRelationshipDTOListForCurrentUser(model);

        return ViewNameConstants.RELATIONSHIP_HOME;
    }


    /**
     * ajouter une relation/connexion ami
     */
    @PostMapping(value = "/contact")
    public String addRelationship(@ModelAttribute("relationshipDTO") @Valid RelationshipDTO relationshipDTOToAdd,
                                  BindingResult bindingResult, Model model) throws PMBException {

        log.info(LogConstants.ADD_RELATIONSHIP_REQUEST_RECEIVED + relationshipDTOToAdd.getFriendEmail());

        if (bindingResult.hasErrors()) {
            log.error(LogConstants.ADD_RELATIONSHIP_REQUEST_NOT_VALID + "\n");
            loadRelationshipDTOListForCurrentUser(model);
            return ViewNameConstants.RELATIONSHIP_HOME;
        }

        //TODO revoir pourquoi le userId est réinitialisé  ==> créer un @ModelAttribute RelationshipDTO ?
        // + revoir le nb de mockito times dans test une fois résolu
        UserDTO currentUser = (UserDTO) model.getAttribute("user");
        relationshipDTOToAdd.setUserId(currentUser.getUserId());

        try {

            Optional<RelationshipDTO> relationshipDTOAdded =
                    relationshipService.createRelationship(relationshipDTOToAdd);

            if (relationshipDTOAdded.isPresent()) {
                log.info(LogConstants.ADD_RELATIONSHIP_REQUEST_OK
                        + relationshipDTOAdded.get().getRelationshipId() + "\n");

                return showHomeRelationship(model);
            }

        } catch (PMBException pmbException) {
            log.error(LogConstants.ADD_RELATIONSHIP_REQUEST_KO + ": " + pmbException.getMessage() + " \n");

            switch (pmbException.getMessage()) {
                case PMBExceptionConstants.ALREADY_EXIST_RELATIONSHIP:
                    bindingResult.rejectValue("friendEmail", "contact.RelationshipDTO.friend.alreadyExists");
                    break;
                case PMBExceptionConstants.DOES_NOT_EXISTS_USER:
                    bindingResult.rejectValue("friendEmail", "contact.RelationshipDTO.email.doesNotExist");
                    break;
                case PMBExceptionConstants.INVALID_FRIEND_EMAIL:
                    bindingResult.rejectValue("friendEmail", "contact.RelationshipDTO.email.invalid");
                    break;
                default:
                    bindingResult.rejectValue("friendEmail", "contact.RelationshipDTO.email.other.error");
                    break;
            }
        }

        loadRelationshipDTOListForCurrentUser(model);
        return ViewNameConstants.RELATIONSHIP_HOME;
    }


    /**
     * charge la liste des relations associées à l'utilisateur connecté
     *
     * @param model model en cours
     * @throws PMBException si l'identifiant transmis est nul
     *                      ou que l'utilisateur n'existe pas
     */
    //TODO à passer en @ModelAttribute ?
    private void loadRelationshipDTOListForCurrentUser(Model model) throws PMBException {

        if (model.getAttribute("user") != null) {
            UserDTO currentUser = (UserDTO) model.getAttribute("user");
            List<RelationshipDTO> relationshipDTOList =
                    relationshipService.getAllRelationshipsForUser(currentUser.getUserId());
            model.addAttribute("relationshipDTOList", relationshipDTOList);
        }
    }
}
