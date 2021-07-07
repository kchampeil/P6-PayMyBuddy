package com.paymybuddy.webapp.controller;

import com.paymybuddy.webapp.constants.BankTransferTypes;
import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.constants.ViewNameConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.BankAccountDTO;
import com.paymybuddy.webapp.model.DTO.BankTransferDTO;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.service.contract.IBankAccountService;
import com.paymybuddy.webapp.service.contract.IBankTransferService;
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
public class BankTransferController {

    private final IBankTransferService bankTransferService;

    private final IBankAccountService bankAccountService;

    @Autowired
    public BankTransferController(IBankTransferService bankTransferService,
                                  IBankAccountService bankAccountService) {
        this.bankTransferService = bankTransferService;
        this.bankAccountService = bankAccountService;
    }


    /**
     * afficher la page d'accueil transfert bancaire
     */
    @GetMapping(value = "/profile")
    public String showHomeBankTransfer(Model model) throws PMBException {

        log.info(LogConstants.GET_BANK_TRANSFER_REQUEST_RECEIVED);

        model.addAttribute("bankTransferDTO", new BankTransferDTO());

        loadNeededListsForCurrentUser(model);

        return ViewNameConstants.BANK_TRANSFER_HOME;
    }


    /**
     * ajouter un transfert bancaire
     */
    @PostMapping(value = "/profile")
    public String addBankTransfer(@ModelAttribute("bankTransferDTO") @Valid BankTransferDTO bankTransferDTOToAdd,
                                  BindingResult bindingResult, Model model) throws PMBException {

        log.info(LogConstants.ADD_BANK_TRANSFER_REQUEST_RECEIVED
                + bankTransferDTOToAdd.getBankAccountId() + " / " + bankTransferDTOToAdd.getDescription()
                + " / " + bankTransferDTOToAdd.getType() + " / " + bankTransferDTOToAdd.getAmount());

        if (bindingResult.hasErrors()) {
            log.error(LogConstants.ADD_BANK_TRANSFER_REQUEST_NOT_VALID + "\n");

            loadNeededListsForCurrentUser(model);
            return ViewNameConstants.BANK_TRANSFER_HOME;
        }

        try {
            Optional<BankTransferDTO> bankTransferDTOAdded =
                    bankTransferService.transferWithBankAccount(bankTransferDTOToAdd);

            if (bankTransferDTOAdded.isPresent()) {
                log.info(LogConstants.ADD_BANK_TRANSFER_REQUEST_OK
                        + bankTransferDTOAdded.get().getBankTransferId() + "\n");

                return showHomeBankTransfer(model);
            }

        } catch (PMBException pmbException) {
            log.error(LogConstants.ADD_BANK_TRANSFER_REQUEST_KO + ": " + pmbException.getMessage() + " \n");

            switch (pmbException.getMessage()) {
                case PMBExceptionConstants.MISSING_INFORMATION_NEW_BANK_TRANSFER:
                    bindingResult.rejectValue("description", "profile.BankTransferDTO.missing.information");
                    break;
                case PMBExceptionConstants.INVALID_BANK_TRANSFER_TYPE:
                    bindingResult.rejectValue("type", "profile.BankTransferDTO.type.invalid");
                    break;
                case PMBExceptionConstants.DOES_NOT_EXISTS_BANK_ACCOUNT:
                    bindingResult.rejectValue("bankAccountId", "profile.BankTransferDTO.bankAccountId.doesNotExist");
                    break;
                case PMBExceptionConstants.INSUFFICIENT_BALANCE:
                    bindingResult.rejectValue("amount", "profile.BankTransferDTO.amount.insufficientBalance");
                    break;
                default:
                    bindingResult.rejectValue("description", "profile.BankTransferDTO.other.error");
                    break;
            }
        }

        loadNeededListsForCurrentUser(model);
        return ViewNameConstants.BANK_TRANSFER_HOME;
    }


    /**
     * charge toutes les listes utiles (liste des comptes bancaires, liste des transferts bancaires, etc.)
     * pour l'utilisateur en cours et les ajoute au modèle
     */
    //TODO à passer en @ModelAttribute ?
    private void loadNeededListsForCurrentUser(Model model) throws PMBException {
        model.addAttribute("typeOfTransferList", BankTransferTypes.values());

        if (model.getAttribute("user") != null) {
            UserDTO currentUser = (UserDTO) model.getAttribute("user");

            List<BankAccountDTO> bankAccountDTOList = bankAccountService.getAllBankAccountsForUser(currentUser.getUserId());
            model.addAttribute("bankAccountDTOList", bankAccountDTOList);

            List<BankTransferDTO> bankTransferDTOList = bankTransferService.getAllBankTransfersForUser(currentUser.getUserId());
            model.addAttribute("bankTransferDTOList", bankTransferDTOList);
        }
    }
}
