package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.BankTransferDTO;

import java.util.Optional;

public interface IBankTransferService {
    Optional<BankTransferDTO> transferFromBankAccount(BankTransferDTO bankTransferDTO) throws PMBException;

    Optional<BankTransferDTO> transferToBankAccount(BankTransferDTO bankTransferDTOToCreate) throws PMBException;
}
