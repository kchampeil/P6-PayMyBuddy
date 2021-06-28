package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.BouchonConstants;
import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.BankAccountDTO;
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
     * afficher la page d'accueil compte bancaire
     */
    @GetMapping(value = "/addBankAccount")
    public String showHomeBankAccount(Model model) throws PMBException {

        model.addAttribute("bankAccountDTO", new BankAccountDTO());

        //TODO-débouchonnage passer le user ID de l'utilisateur connecté
        loadBankAccountDTOList(model, BouchonConstants.USER_BOUCHON);

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

            loadBankAccountDTOList(model, BouchonConstants.USER_BOUCHON);//TODO-débouchonnage
            return ViewNameConstants.BANK_ACCOUNT_HOME;
        }

        //TODO revoir pour mettre ça dans le bankAccountService une fois spring security en place ?
        bankAccountDTOToAdd.setUserId(BouchonConstants.USER_BOUCHON); //TODO-débouchonnage

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
                default:
                    bindingResult.rejectValue("iban", "addBankAccount.BankAccountDTO.iban.other.error");
            }
        }

        //TODO-débouchonnage passer le user ID ensuite
        loadBankAccountDTOList(model, BouchonConstants.USER_BOUCHON);
        return ViewNameConstants.BANK_ACCOUNT_HOME;
    }


    private void loadBankAccountDTOList(Model model, Long userId) throws PMBException {
        List<BankAccountDTO> bankAccountDTOList = bankAccountService.getAllBankAccountsForUser(userId);
        model.addAttribute("bankAccountDTOList", bankAccountDTOList);
    }
}
