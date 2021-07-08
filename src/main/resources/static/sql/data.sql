USE pmbprod;

--
-- Alimentation table `user`
-- mdp correspondants aux utilisateurs :
-- balthazar.picsou@pmb.com -> mdp : BP2021!
-- riri.picsou@pmb.com -> mdp : RP2021!
-- fifi.picsou@pmb.com -> mdp : FP2021!
-- loulou.picsou@pmb.com -> mdp : LP2021!
-- gordon.gekko@pmb.com -> mdp : GG2021!
-- bud.fox@pmb.com -> mdp : BF2021!
-- archibald.gripsou@pmb.com -> mdp : AG2021!
--
INSERT INTO `user` (user_id, balance, email, firstname, lastname, password)
VALUES (1, 10000.00, 'balthazar.picsou@pmb.com', 'Balthazar', 'PICSOU',
        '$2a$10$09b1QmB5d0vGA1be23KtkOHzCuGavzrXlK0evpRzs4L6MTDXQHccK'),
       (2, 0.00, 'riri.picsou@pmb.com', 'Riri', 'PICSOU',
        '$2a$10$Ns4KPYtcC4kO8f76PYlNJu3YDikgbFpf27sPZUaOwD1Sx0j4Fq.fq'),
       (3, 50.00, 'fifi.picsou@pmb.com', 'Fifi', 'PICSOU',
        '$2a$10$DuPAVShurJOsfYfE886FLODXVYXhZ3oD20XY9pe8s56mqK5r1tsKO'),
       (4, 0.00, 'loulou.picsou@pmb.com', 'Loulou', 'PICSOU',
        '$2a$10$nUfb9mM5xkS7uivK9JUyoOdOYUhJ2mft5RMA6oTb/Yi6HN3tWQ9Ta'),
       (5, 0.00, 'gordon.gekko@pmb.com', 'Gordon', 'GEKKO',
        '$2a$10$Ri/p5N6MIVvBD7v1jUcjPu26IyHN3UOZNQQ6ixPtEu.6B.8Y32/ju'),
       (6, 0.00, 'bud.fox@pmb.com', 'Bud', 'FOX', '$2a$10$SXAfEYU./RY15lAbEgX20ehSQVylulnyoauEkA87eM6/K1Sk7l4AW'),
       (7, 0.01, 'archibald.gripsou@pmb.com', 'Archibald', 'GRIPSOU',
        '$2a$10$wyl51hYUYTXjrfxYY5AA0.b1E1GNJ4CdsPR1qXgjDe0xEjH8.BPla');


--
-- Alimentation table `bank_account`
--
INSERT INTO `bank_account` (bank_account_id, iban, name, user_id)
VALUES (1, 'FR7611112222333344445555652', 'Compte principal Picsou', 1),
       (2, 'FR0011112222333344445555666', 'Compte complémentaire Picsou', 1),
       (3, 'FR6666666666666666666666666', 'Compte à supprimer Picsou', 1);

--
-- Alimentation table `bank_transfer`
--
INSERT INTO `bank_transfer` (bank_transfer_id, amount, date, description, type, bank_account_id)
VALUES (1, 10000.00, '2021-07-01 09:54:32', 'Alimentation initiale du compte', 1, 1);

--
-- Alimentation table `relationship`
--
INSERT INTO `relationship` (relationship_id, friend_id, user_id)
VALUES (1, 3, 1),
       (2, 7, 1);

--
-- Alimentation table `transaction`
--
INSERT INTO `transaction` (transaction_id, amount_fee_excl, date, description, fee_amount, fee_billed, relationship_id)
VALUES (1, 50.00, '2021-07-01 09:57:34', 'Argent de poche', 0.25, _binary '\0', 1),
       (2, 0.01, '2021-07-01 09:57:48', 'C\'est déjà beaucoup trop', 0.00, _binary '\0', 2);
