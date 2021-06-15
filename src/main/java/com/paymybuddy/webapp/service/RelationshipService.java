package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.RelationshipDTO;
import com.paymybuddy.webapp.model.Relationship;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.RelationshipRepository;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.contract.IRelationshipService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class RelationshipService implements IRelationshipService {

    private final RelationshipRepository relationshipRepository;

    private final UserRepository userRepository;

    @Autowired
    RelationshipService(RelationshipRepository relationshipRepository, UserRepository userRepository) {
        this.relationshipRepository = relationshipRepository;
        this.userRepository = userRepository;
    }


    /**
     * création d une relation entre un utilisateur et un autre utilisateur ami
     *
     * @param relationshipDTOToCreate relation à créer
     * @return objet RelationshipDTO contenant la relation créée
     * @throws PMBException si la relation existe déjà,
     *                      que un des utilisateurs n existe pas
     *                      ou que des données sont manquantes
     */
    @Override
    public Optional<RelationshipDTO> createRelationship(RelationshipDTO relationshipDTOToCreate) throws PMBException {
        Optional<RelationshipDTO> createdRelationshipDTO;

        //vérifie qu il ne manque pas d informations
        if (relationshipDTOToCreate.isValid()) {

            //vérifie que les deux utilisateurs existent bien
            Optional<User> user = userRepository.findById(relationshipDTOToCreate.getUserId());
            Optional<User> friend = userRepository.findById(relationshipDTOToCreate.getFriendId());

            if (user.isPresent() && friend.isPresent()) {

                //vérifie que la relation n existe pas déjà pour l utilisateur
                if (!relationshipRepository
                        .findByUserAndFriend(user.get(), friend.get()).isPresent()) {

                    //mappe le DTO dans le DAO,
                    // puis la nouvelle relation est sauvegardée en base avant mappage inverse du DAO dans le DTO
                    try {
                        Relationship relationshipToCreate = new Relationship();
                        relationshipToCreate.setUser(user.get());
                        relationshipToCreate.setFriend(friend.get());
                        Relationship createdRelationship =
                                relationshipRepository.save(relationshipToCreate);

                        ModelMapper modelMapper = new ModelMapper();
                        createdRelationshipDTO =
                                Optional.ofNullable(modelMapper.map(createdRelationship, RelationshipDTO.class));
                        log.info(LogConstants.CREATE_RELATIONSHIP_OK + relationshipDTOToCreate.getRelationshipId());

                    } catch (Exception exception) {
                        log.error(LogConstants.CREATE_RELATIONSHIP_ERROR + relationshipDTOToCreate.getUserId()
                                + " // " + relationshipDTOToCreate.getFriendId());
                        throw exception;
                    }
                } else {
                    log.error(LogConstants.CREATE_RELATIONSHIP_ERROR
                            + PMBExceptionConstants.ALREADY_EXIST_RELATIONSHIP
                            + relationshipDTOToCreate.getUserId() + " // " + relationshipDTOToCreate.getFriendId());
                    throw new PMBException(PMBExceptionConstants.ALREADY_EXIST_RELATIONSHIP
                            + relationshipDTOToCreate.getUserId() + " // " + relationshipDTOToCreate.getFriendId());
                }

            } else {
                String msgComplement;
                if (!user.isPresent()) {
                    if (!friend.isPresent()) {
                        msgComplement = relationshipDTOToCreate.getUserId().toString()
                                + " // " + relationshipDTOToCreate.getFriendId().toString();
                    } else {
                        msgComplement = relationshipDTOToCreate.getUserId().toString();
                    }
                } else {
                    msgComplement = relationshipDTOToCreate.getFriendId().toString();
                }
                log.error(LogConstants.CREATE_RELATIONSHIP_ERROR + PMBExceptionConstants.DOES_NOT_EXISTS_USER + msgComplement);
                throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_USER + msgComplement);
            }


        } else {
            log.error(LogConstants.CREATE_RELATIONSHIP_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_NEW_RELATIONSHIP
                    + "for: " + relationshipDTOToCreate.getUserId()
                    + " // " + relationshipDTOToCreate.getFriendId());
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_RELATIONSHIP);
        }

        return createdRelationshipDTO;
    }


    /**
     * récupération de la liste de toutes les relations ("amis") d'un utilisateur donné
     *
     * NB: ne récupère que la liste des "amis" déclarés par l'utilisateur.
     * Les relations où l'utilisateur est déclaré comme "ami" ne sont pas restituées
     *
     * @param userId id de l utilisateur dont on souhaite à récupérer la liste des relations
     * @return la liste des relations (DTO)
     */
    @Override
    public List<RelationshipDTO> getAllRelationshipsForUser(Long userId) throws PMBException {
        List<RelationshipDTO> relationshipDTOList = new ArrayList<>();

        if (userId != null) {
            Optional<User> user = userRepository.findById(userId);

            if (user.isPresent()) {
                List<Relationship> relationshipList = relationshipRepository.findAllByUser_UserId(userId);
                ModelMapper modelMapper = new ModelMapper();
                relationshipList.forEach(relationship ->
                        relationshipDTOList
                                .add(modelMapper.map(relationship, RelationshipDTO.class)));
                log.info(LogConstants.LIST_RELATIONSHIP_OK + relationshipDTOList.size());

            } else {
                log.error(LogConstants.LIST_RELATIONSHIP_ERROR
                        + PMBExceptionConstants.DOES_NOT_EXISTS_USER + userId);
                throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_USER + userId);
            }

        } else {
            log.error(LogConstants.LIST_RELATIONSHIP_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_LIST_RELATIONSHIP);
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_LIST_RELATIONSHIP);
        }

        return relationshipDTOList;
    }
}
