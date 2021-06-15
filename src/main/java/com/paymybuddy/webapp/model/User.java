package com.paymybuddy.webapp.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * la classe User contient les informations du compte de l'utilisateur dans PayMyBuddy
 */
@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="user_id", nullable = false)
    private Long userId;

    @Column(name="email", nullable = false, length = 256)
    private String email;

    @Column(name="firstname", nullable = false, length = 64)
    private String firstname;

    @Column(name="lastname", nullable = false, length = 64)
    private String lastname;

    @Column(name = "password", nullable = false, length = 64)
    private String password;

    @Column(name = "balance", nullable = false, precision = 7, scale = 2)
    private BigDecimal balance=BigDecimal.ZERO;

}
