package com.paymybuddy.webapp.service.contract;

import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.RelationshipDTO;

import java.util.List;
import java.util.Optional;

public interface IRelationshipService {

    Optional<RelationshipDTO> createRelationship(RelationshipDTO relationshipDTO) throws PMBException;

    List<RelationshipDTO> getAllRelationshipsForUser(Long userId) throws PMBException;
}
