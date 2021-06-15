package com.paymybuddy.webapp.model;

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

/**
 * La classe BankAccount contient les détails d'un compte bancaire (nom/IBAN)
 * associé à l'utilisateur de PayMyBuddy
 * Un utilisateur peut avoir plusieurs comptes bancaires associés
 */
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "bank_account")
public class BankAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "bank_account_id", nullable = false)
    private Long bankAccountId;

    @Column(name = "iban", nullable = false, length = 34)
    private String iban;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
