package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.BankAccountDTO;

import java.util.Optional;

public interface IBankAccountService {
    Optional<BankAccountDTO> createBankAccount(BankAccountDTO bankAccountDTOToCreate) throws PMBException;
}
