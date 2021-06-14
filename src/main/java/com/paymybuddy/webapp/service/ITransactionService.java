package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.TransactionDTO;

import java.util.Optional;

public interface ITransactionService {
    Optional<TransactionDTO> transferToFriend(TransactionDTO transactionDTO) throws PMBException;
}
