package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.RelationshipDTO;
import com.paymybuddy.webapp.model.DTO.TransactionDTO;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.service.contract.IRelationshipService;
import com.paymybuddy.webapp.service.contract.ITransactionService;
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
public class TransactionController {

    private final IRelationshipService relationshipService;

    private final ITransactionService transactionService;

    @Autowired
    public TransactionController(IRelationshipService relationshipService,
                                 ITransactionService transactionService) {
        this.relationshipService = relationshipService;
        this.transactionService = transactionService;
    }

    /**
     * initialise le modèle avec la liste des transactions associées à l'utilisateur courant,
     * la liste des relations de l'utilisateur courant
     * et avec un transactionDTO initialisé
     */
    @ModelAttribute
    public void addTransactionAttributesToModel(Model model) throws PMBException {

        if (model.getAttribute("user") != null) {
            UserDTO currentUser = (UserDTO) model.getAttribute("user");

            List<RelationshipDTO> relationshipDTOList = relationshipService.getAllRelationshipsForUser(currentUser.getUserId());

            model.addAttribute("relationshipDTOList", relationshipDTOList);

            List<TransactionDTO> transactionDTOList = transactionService.getAllTransactionsForUser(currentUser.getUserId());
            model.addAttribute("transactionDTOList", transactionDTOList);
        }

        TransactionDTO transactionDTO = new TransactionDTO();
        model.addAttribute("transactionDTO", transactionDTO);
    }


    /**
     * afficher la page d'accueil transaction
     */
    @GetMapping(value = "/transfer")
    public String showHomeTransaction() throws PMBException {

        log.info(LogConstants.GET_TRANSACTION_REQUEST_RECEIVED);

        //TODO V2 gérer la pagination de la liste de transactions

        return ViewNameConstants.TRANSACTION_HOME;
    }


    /**
     * payer un ami
     */
    @PostMapping(value = "/transfer")
    public String addTransaction(@ModelAttribute("transactionDTO") @Valid TransactionDTO transactionDTOToAdd,
                                 BindingResult bindingResult, Model model) {

        log.info(LogConstants.ADD_TRANSACTION_REQUEST_RECEIVED
                + transactionDTOToAdd.getRelationshipId() + " / " + transactionDTOToAdd.getDescription()
                + " / " + transactionDTOToAdd.getAmountFeeExcluded());

        if (bindingResult.hasErrors()) {
            log.error(LogConstants.ADD_TRANSACTION_REQUEST_NOT_VALID + "\n");
            return ViewNameConstants.TRANSACTION_HOME;
        }

        try {
            Optional<TransactionDTO> transactionDTOAdded =
                    transactionService.transferToFriend(transactionDTOToAdd);

            if (transactionDTOAdded.isPresent()) {
                log.info(LogConstants.ADD_TRANSACTION_REQUEST_OK
                        + transactionDTOAdded.get().getTransactionId() + "\n");

               /* met à jour les éléments du modèle
                avant de réafficher la page pour une autre saisie */
                addTransactionAttributesToModel(model);
                return showHomeTransaction();
            }

        } catch (PMBException pmbException) {
            log.error(LogConstants.ADD_TRANSACTION_REQUEST_KO + ": " + pmbException.getMessage() + " \n");

            switch (pmbException.getMessage()) {
                case PMBExceptionConstants.MISSING_INFORMATION_NEW_TRANSACTION:
                    bindingResult.rejectValue("description",
                            "transfer.TransactionDTO.missing.information");
                    break;
                case PMBExceptionConstants.DOES_NOT_EXISTS_RELATIONSHIP:
                    bindingResult.rejectValue("relationshipId",
                            "transfer.TransactionDTO.relationshipId.doesNotExist");
                    break;
                case PMBExceptionConstants.INSUFFICIENT_BALANCE:
                    bindingResult.rejectValue("amountFeeExcluded",
                            "transfer.TransactionDTO.amountFeeExcluded.insufficientBalance");
                    break;
                default:
                    bindingResult.rejectValue("description",
                            "transfer.TransactionDTO.other.error");
                    break;
            }
        }

        return ViewNameConstants.TRANSACTION_HOME;
    }
}
