package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.BankAccountDTO;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.service.contract.IBankAccountService;
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
public class BankAccountController {

    private final IBankAccountService bankAccountService;

    @Autowired
    public BankAccountController(IBankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    /**
     * initialise le modèle avec la liste des comptes bancaires associés à l'utilisateur courant
     * et avec un bankAccountDTO initialisé avec l'id de l'utilisateur courant
     */
    @ModelAttribute
    public void addBankAccountAttributesToModel(Model model) throws PMBException {

        if (model.getAttribute("user") != null) {
            UserDTO currentUser = (UserDTO) model.getAttribute("user");

            List<BankAccountDTO> bankAccountDTOList =
                    bankAccountService.getAllBankAccountsForUser(currentUser.getUserId());
            model.addAttribute("bankAccountDTOList", bankAccountDTOList);

            BankAccountDTO bankAccountDTO = new BankAccountDTO();
            bankAccountDTO.setUserId(currentUser.getUserId());
            model.addAttribute("bankAccountDTO", bankAccountDTO);
        }
    }


    /**
     * afficher la page d'accueil compte bancaire
     */
    @GetMapping(value = "/addBankAccount")
    public String showHomeBankAccount() throws PMBException {

        log.info(LogConstants.GET_BANK_ACCOUNT_REQUEST_RECEIVED);

        //TODO V2 gérer la pagination de la liste des comptes bancaires

        return ViewNameConstants.BANK_ACCOUNT_HOME;
    }

    /**
     * ajouter un compte bancaire
     */
    @PostMapping(value = "/addBankAccount")
    public String addBankAccount(@ModelAttribute("bankAccountDTO") @Valid BankAccountDTO bankAccountDTOToAdd,
                                 BindingResult bindingResult, Model model) {

        log.info(LogConstants.ADD_BANK_ACCOUNT_REQUEST_RECEIVED
                + bankAccountDTOToAdd.getIban() + " / " + bankAccountDTOToAdd.getName());

        if (bindingResult.hasErrors()) {
            log.error(LogConstants.ADD_BANK_ACCOUNT_REQUEST_NOT_VALID + "\n");
            return ViewNameConstants.BANK_ACCOUNT_HOME;
        }

        try {
            Optional<BankAccountDTO> bankAccountDTOAdded =
                    bankAccountService.createBankAccount(bankAccountDTOToAdd);

            if (bankAccountDTOAdded.isPresent()) {
                log.info(LogConstants.ADD_BANK_ACCOUNT_REQUEST_OK
                        + bankAccountDTOAdded.get().getBankAccountId() + "\n");

                /* met à jour les éléments du modèle
                avant de réafficher la page pour une autre saisie */
                addBankAccountAttributesToModel(model);
                return showHomeBankAccount();
            }

        } catch (PMBException pmbException) {
            log.error(LogConstants.ADD_BANK_ACCOUNT_REQUEST_KO + ": " + pmbException.getMessage() + " \n");

            switch (pmbException.getMessage()) {
                case PMBExceptionConstants.ALREADY_EXIST_BANK_ACCOUNT:
                    bindingResult.rejectValue("iban", "addBankAccount.BankAccountDTO.iban.alreadyExists");
                    break;
                case PMBExceptionConstants.INVALID_IBAN:
                    bindingResult.rejectValue("iban", "addBankAccount.BankAccountDTO.iban.invalid");
                    break;
                case PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_ACCOUNT:
                    bindingResult.rejectValue("iban", "addBankAccount.BankAccountDTO.iban.or.name.missing.information");
                    break;
                default:
                    bindingResult.rejectValue("iban", "addBankAccount.BankAccountDTO.iban.other.error");
                    break;
            }
        }

        return ViewNameConstants.BANK_ACCOUNT_HOME;
    }
}

