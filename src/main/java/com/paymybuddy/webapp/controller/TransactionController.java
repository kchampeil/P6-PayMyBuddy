package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.BouchonConstants;
import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.RelationshipDTO;
import com.paymybuddy.webapp.model.DTO.TransactionDTO;
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
    public TransactionController(IRelationshipService relationshipService, ITransactionService transactionService) {
        this.relationshipService = relationshipService;
        this.transactionService = transactionService;
    }

    /**
     * afficher la page d'accueil transaction
     */
    @GetMapping(value = "/transfer")
    // TODO public String showHomeTransaction(Model model, @RequestParam(name = "page", defaultValue = "0") int page) throws PMBException {
    public String showHomeTransaction(Model model) throws PMBException {

        model.addAttribute("transactionDTO", new TransactionDTO());

        //TODO-débouchonnage passer le user ID de l'utilisateur connecté
        loadNeededLists(model, BouchonConstants.USER_BOUCHON);
        //TODO récupérer et afficher la balance du compte ?

        return ViewNameConstants.TRANSACTION_HOME;

        /* TODO voir la pagination
        Page<TransactionDTO> transactionDTOList=transactionService.getAllTransactionsForUser(20L, PageRequest.of(0,3));
        model.addAttribute("transactionDTOList", transactionDTOList.getContent());
        model.addAttribute("pages", new int[transactionDTOList.getTotalPages()]);
        return ViewNameConstants.TRANSACTION_HOME;

         */

    }


    /**
     * payer un ami
     */
    @PostMapping(value = "/transfer")
    public String addTransaction(@ModelAttribute("transactionDTO") @Valid TransactionDTO transactionDTOToAdd,
                                 BindingResult bindingResult, Model model) throws PMBException {

        log.info(LogConstants.ADD_TRANSACTION_REQUEST_RECEIVED
                + transactionDTOToAdd.getRelationshipId() + " / " + transactionDTOToAdd.getDescription()
                + " / " + transactionDTOToAdd.getAmountFeeExcluded());

        if (bindingResult.hasErrors()) {
            log.error(LogConstants.ADD_TRANSACTION_REQUEST_NOT_VALID + "\n");

            loadNeededLists(model, BouchonConstants.USER_BOUCHON);//TODO-débouchonnage
            return ViewNameConstants.TRANSACTION_HOME;
        }

        try {
            Optional<TransactionDTO> transactionDTOAdded =
                    transactionService.transferToFriend(transactionDTOToAdd);

            if (transactionDTOAdded.isPresent()) {
                log.info(LogConstants.ADD_TRANSACTION_REQUEST_OK
                        + transactionDTOAdded.get().getTransactionId() + "\n");

                return showHomeTransaction(model);
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

        //TODO-débouchonnage passer le user ID ensuite
        loadNeededLists(model, BouchonConstants.USER_BOUCHON);
        return ViewNameConstants.TRANSACTION_HOME;
    }


    /**
     * charge toutes les listes utiles (liste des connexions, liste des transactions)
     * pour l'utilisateur en cours et les ajoute au modèle
     */
    private void loadNeededLists(Model model, Long userId) throws PMBException {
        List<RelationshipDTO> relationshipDTOList = relationshipService.getAllRelationshipsForUser(userId);
        model.addAttribute("relationshipDTOList", relationshipDTOList);

        List<TransactionDTO> transactionDTOList = transactionService.getAllTransactionsForUser(userId);
        model.addAttribute("transactionDTOList", transactionDTOList);
    }
}
