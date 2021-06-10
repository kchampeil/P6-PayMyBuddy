package com.paymybuddy.webapp.repository;

import com.paymybuddy.webapp.model.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {
    Optional<BankAccount> findByIbanAndUser_UserId(String iban, long userId);

    List<BankAccount> findAllByUser_UserId(Long userId);
}
