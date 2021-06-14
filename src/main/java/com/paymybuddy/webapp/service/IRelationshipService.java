package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.RelationshipDTO;

import java.util.List;
import java.util.Optional;

public interface IRelationshipService {
    Optional<RelationshipDTO> createRelationship(RelationshipDTO relationshipDTOToCreate) throws PMBException;

    List<RelationshipDTO> getAllRelationshipsForUser(Long userId) throws PMBException;
}
