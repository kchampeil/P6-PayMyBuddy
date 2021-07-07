package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.BankAccountDTO;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.service.PMBUserDetailsService;
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

    private final PMBUserDetailsService pmbUserDetailsService;

    @Autowired
    public BankAccountController(IBankAccountService bankAccountService, PMBUserDetailsService pmbUserDetailsService) {
        this.bankAccountService = bankAccountService;
        this.pmbUserDetailsService = pmbUserDetailsService;
    }


    /**
     * afficher la page d'accueil compte bancaire
     */
    @GetMapping(value = "/addBankAccount")
    public String showHomeBankAccount(Model model) throws PMBException {

        log.info(LogConstants.GET_BANK_ACCOUNT_REQUEST_RECEIVED);

        model.addAttribute("bankAccountDTO", new BankAccountDTO());

        //récupération de la liste des comptes bancaires associés à l'utilisateur connecté
        loadBankAccountDTOListForCurrentUser(model);

        return ViewNameConstants.BANK_ACCOUNT_HOME;
    }

    /**
     * ajouter un compte bancaire
     */
    @PostMapping(value = "/addBankAccount")
    public String addBankAccount(@ModelAttribute("bankAccountDTO") @Valid BankAccountDTO bankAccountDTOToAdd,
                                 BindingResult bindingResult, Model model) throws PMBException {

        log.info(LogConstants.ADD_BANK_ACCOUNT_REQUEST_RECEIVED
                + bankAccountDTOToAdd.getIban() + " / " + bankAccountDTOToAdd.getName());

        if (bindingResult.hasErrors()) {
            log.error(LogConstants.ADD_BANK_ACCOUNT_REQUEST_NOT_VALID + "\n");

            loadBankAccountDTOListForCurrentUser(model);

            return ViewNameConstants.BANK_ACCOUNT_HOME;
        }

        //TODO revoir pourquoi le userId est réinitialisé ==> créer un @ModelAttribute BankAccountDTO ?
        // + revoir le nb de mockito times dans test une fois résolu
        bankAccountDTOToAdd.setUserId(pmbUserDetailsService.getCurrentUser().getUserId());

        try {

            Optional<BankAccountDTO> bankAccountDTOAdded =
                    bankAccountService.createBankAccount(bankAccountDTOToAdd);

            if (bankAccountDTOAdded.isPresent()) {
                log.info(LogConstants.ADD_BANK_ACCOUNT_REQUEST_OK
                        + bankAccountDTOAdded.get().getBankAccountId() + "\n");

                return showHomeBankAccount(model);
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

        loadBankAccountDTOListForCurrentUser(model);
        return ViewNameConstants.BANK_ACCOUNT_HOME;
    }


    /**
     * charge la liste des comptes bancaires associés à l'utilisateur connecté
     *
     * @param model model en cours
     * @throws PMBException si l'identifiant transmis est nul
     *                      ou que l'utilisateur n'existe pas
     */
    //TODO à passer en @ModelAttribute ?
    private void loadBankAccountDTOListForCurrentUser(Model model) throws PMBException {
        if (model.getAttribute("user") != null) {
            User currentUser = (User) model.getAttribute("user");

            List<BankAccountDTO> bankAccountDTOList =
                    bankAccountService.getAllBankAccountsForUser(currentUser.getUserId());
            model.addAttribute("bankAccountDTOList", bankAccountDTOList);
        }
    }
}
