package com.paymybuddy.webapp.repository;

import com.paymybuddy.webapp.model.Relationship;
import com.paymybuddy.webapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RelationshipRepository extends JpaRepository<Relationship, Long> {
    Optional<Relationship> findByUserAndFriend(User user, User friend);
}
