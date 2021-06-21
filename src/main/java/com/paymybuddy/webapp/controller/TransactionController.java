package com.paymybuddy.webapp.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

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
    //TODO à revoir
    @GetMapping(value = "/transfer")
    public String showHomeTransaction(Model model, @RequestParam(name = "page", defaultValue = "0") int page) throws PMBException {

        model.addAttribute("transactionDTO", new TransactionDTO());

        List<RelationshipDTO> relationshipDTOList = relationshipService.getAllRelationshipsForUser(20L);
        //TODO passer le user ID ensuite
        //TODO rappatrier aussi le nom de l'ami dans le DTO

        model.addAttribute("relationshipDTOList", relationshipDTOList);

        List<TransactionDTO> transactionDTOList = transactionService.getAllTransactionsForUser(20L);
        //Page<TransactionDTO> transactionDTOList=transactionService.getAllTransactionsForUser(20L, PageRequest.of(0,3));
        //TODO passer le user ID ensuite
        model.addAttribute("transactionDTOList", transactionDTOList);
        //model.addAttribute("transactionDTOList", transactionDTOList.getContent());
        //model.addAttribute("pages", new int[transactionDTOList.getTotalPages()]);
        return ViewNameConstants.TRANSACTION_HOME;

    }


    /**
     * payer un ami
     */
    //TODO à revoir
    @GetMapping(value = "/payFriend")
    public String addTransaction() {

        return ViewNameConstants.UNDER_CONSTRUCTION;

    }
}
