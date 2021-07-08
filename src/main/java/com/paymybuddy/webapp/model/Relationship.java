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
 * La classe Relationship contient les liens déclaré par un utilisateur avec d'autres
 * utilisateurs de l'application ("amis")
 * Un utilisateur peut avoir plusieurs "amis" déclarés
 */
@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(name = "relationship")
public class Relationship {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "relationship_id", nullable = false)
    private Long relationshipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

}
