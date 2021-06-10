package com.paymybuddy.webapp.repository;

import com.paymybuddy.webapp.model.BankTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface BankTransferRepository extends JpaRepository<BankTransfer, Long> {
    Optional<BankTransfer> findByDateAndBankAccount_BankAccountId(LocalDateTime date, Long bankAccountId);
}
