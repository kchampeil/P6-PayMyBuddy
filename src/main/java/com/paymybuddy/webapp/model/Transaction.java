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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * La classe Transaction permet d'enregistrer un transfert entre un compte utilisateur
 * et le compte d'un utilisateur déclaré comme ami par le premier dans PayMyBuddy
 *
 * 'feeBilled' permet d'identifier les frais non encore facturés (true si facturés)
 *
 */
@Getter
@Setter
@RequiredArgsConstructor
@Entity
@Table(name = "transaction")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "transaction_id", nullable = false)
    private Long transactionId;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "description", nullable = false, length = 128)
    private String description;

    @Column(name = "amount_fee_excl", nullable = false, precision = 7, scale = 2)
    private BigDecimal amountFeeExcluded = BigDecimal.ZERO;

    @Column(name = "fee_amount", nullable = false, precision = 7, scale = 2)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "fee_billed", nullable = false)
    private boolean feeBilled;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_id", nullable = false)
    private Relationship relationship;
}
