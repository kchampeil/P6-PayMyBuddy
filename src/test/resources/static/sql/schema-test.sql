--
-- Création de la base pmbprod
--
CREATE
    DATABASE IF NOT EXISTS pmbtest;

USE
    pmbtest;

--
-- Suppression des tables si elles existent
--
DROP TABLE IF EXISTS `bank_transfer`;
DROP TABLE IF EXISTS `bank_account`;
DROP TABLE IF EXISTS `transaction`;
DROP TABLE IF EXISTS `relationship`;
DROP TABLE IF EXISTS `user`;

--
-- Création de la structure de la table `user`
--
CREATE TABLE IF NOT EXISTS `user`
(
    `user_id`   bigint        NOT NULL,
    `balance`   decimal(7, 2) NOT NULL,
    `email`     varchar(256)  NOT NULL,
    `firstname` varchar(64)   NOT NULL,
    `lastname`  varchar(64)   NOT NULL,
    `password`  varchar(256)  NOT NULL,
    PRIMARY KEY (`user_id`),
    CONSTRAINT user_email_idx UNIQUE (`email`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

CREATE UNIQUE INDEX user_idx
    ON user (email);

--
-- Création de la structure de la table `bank_account`
--
CREATE TABLE IF NOT EXISTS `bank_account`
(
    `bank_account_id` bigint      NOT NULL,
    `iban`            varchar(34) NOT NULL,
    `name`            varchar(64) NOT NULL,
    `user_id`         bigint      NOT NULL,
    PRIMARY KEY (`bank_account_id`),
    KEY `user_bank_account_fk` (`user_id`),
    CONSTRAINT `user_bank_account_fk`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

--
-- Création de la structure de la table `bank_transfer`
--
CREATE TABLE IF NOT EXISTS `bank_transfer`
(
    `bank_transfer_id` bigint        NOT NULL,
    `amount`           decimal(7, 2) NOT NULL,
    `date`             datetime      NOT NULL,
    `description`      varchar(128)  NOT NULL,
    `type`             int           NOT NULL,
    `bank_account_id`  bigint        NOT NULL,
    PRIMARY KEY (`bank_transfer_id`),
    KEY `bank_account_bank_transfer_fk` (`bank_account_id`),
    CONSTRAINT `bank_account_bank_transfer_fk`
        FOREIGN KEY (`bank_account_id`) REFERENCES `bank_account` (`bank_account_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

--
-- Création de la structure de la table `relationship`
--
CREATE TABLE IF NOT EXISTS `relationship`
(
    `relationship_id` bigint NOT NULL,
    `friend_id`       bigint NOT NULL,
    `user_id`         bigint NOT NULL,
    PRIMARY KEY (`relationship_id`),
    KEY `user_relationship_friend_fk` (`friend_id`),
    KEY `user_relationship_user_fk` (`user_id`),
    CONSTRAINT `user_relationship_user_fk`
        FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`),
    CONSTRAINT `user_relationship_friend_fk`
        FOREIGN KEY (`friend_id`) REFERENCES `user` (`user_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;

--
-- Création de la structure de la table `transaction`
--
CREATE TABLE IF NOT EXISTS `transaction`
(
    `transaction_id`  bigint        NOT NULL,
    `amount_fee_excl` decimal(7, 2) NOT NULL,
    `date`            datetime      NOT NULL,
    `description`     varchar(128)  NOT NULL,
    `fee_amount`      decimal(7, 2) NOT NULL,
    `fee_billed`      bit(1)        NOT NULL,
    `relationship_id` bigint        NOT NULL,
    PRIMARY KEY (`transaction_id`),
    KEY `relationship_transaction_fk` (`relationship_id`),
    CONSTRAINT `relationship_transaction_fk`
        FOREIGN KEY (`relationship_id`) REFERENCES `relationship` (`relationship_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
