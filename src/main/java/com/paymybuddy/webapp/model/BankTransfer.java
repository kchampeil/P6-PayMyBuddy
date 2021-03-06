package com.paymybuddy.webapp.model;

import com.paymybuddy.webapp.constants.BankTransferTypes;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * La classe BankTransfer permet d'enregistrer un transfert entre un compte bancaire d'un utilisateur
 * et le compte de l'utilisateur dans PayMyBuddy
 * le 'type' CREDIT concerne un mouvement du compte bancaire vers le compte utilisateur
 * le 'type' DEBIT concerne un mouvement du compte utilisateur vers le compte bancaire
 */
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "bank_transfer")
public class BankTransfer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "bank_transfer_id", nullable = false)
    private Long bankTransferId;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "description", nullable = false, length = 128)
    private String description;

    @Column(name = "amount", nullable = false, precision = 7, scale = 2)
    private BigDecimal amount = BigDecimal.ZERO;

    @Column(name = "type", nullable = false)
    private BankTransferTypes type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_account_id", nullable = false)
    private BankAccount bankAccount;
}
