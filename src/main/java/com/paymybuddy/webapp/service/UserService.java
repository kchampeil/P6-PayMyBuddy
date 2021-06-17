package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.constants.LogConstants;
import com.paymybuddy.webapp.constants.PMBExceptionConstants;
import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.model.User;
import com.paymybuddy.webapp.repository.UserRepository;
import com.paymybuddy.webapp.service.contract.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@Transactional
public class UserService implements IUserService {

    private final UserRepository userRepository;

    @Autowired
    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * création d un utilisateur en base
     *
     * @param userDTOToCreate utilisateur à créer
     * @return objet UserDTO contenant l utilisateur créé
     * @throws PMBException si l utilisateur existe déjà,
     *                      que son email est invalide
     *                      ou que des données sont manquantes
     */
    @Override
    public Optional<UserDTO> createUser(UserDTO userDTOToCreate) throws PMBException {

        Optional<UserDTO> createdUserDTO = Optional.empty();

        if (checksBeforeCreatingUser(userDTOToCreate)) {
            // mappe le DTO dans le DAO,
            // puis le nouvel utilisateur sauvegardé en base avant mappage inverse du DAO dans le DTO
            ModelMapper modelMapper = new ModelMapper();
            User createdUser;

            try {
                createdUser = userRepository.save(modelMapper.map(userDTOToCreate, User.class));

            } catch (Exception exception) {
                log.error(LogConstants.CREATE_USER_ERROR + userDTOToCreate.getEmail());
                throw exception;
            }

            createdUserDTO = Optional.ofNullable(modelMapper.map((createdUser), UserDTO.class));
            log.info(LogConstants.CREATE_USER_OK + createdUserDTO.orElse(null).getUserId());
        }

        return createdUserDTO;
    }


    /**
     * vérifie que les informations transmises sont correctes en amont de la création de l'utilisateur
     *
     * @param userDTOToCreate contient les informations sur l'utilisateur' à créer
     * @return true si tout est correct
     * @throws PMBException si des données sont manquantes
     *                      ou que l'email est invalide
     *                      ou que l utilisateur existe déjà
     */
    private boolean checksBeforeCreatingUser(UserDTO userDTOToCreate) throws PMBException {
        // vérifie qu il ne manque pas d informations
        if (!userDTOToCreate.isValid()) {
            log.error(LogConstants.CREATE_USER_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_NEW_USER + "for: " + userDTOToCreate.getEmail());
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_NEW_USER);
        }

        // vérifie que l email est valide
        if (!userDTOToCreate.hasValidEmail()) {
            log.error(LogConstants.CREATE_USER_ERROR
                    + PMBExceptionConstants.INVALID_EMAIL + userDTOToCreate.getEmail());
            throw new PMBException(PMBExceptionConstants.INVALID_EMAIL + userDTOToCreate.getEmail());
        }

        // vérifie que l utilisateur n existe pas déjà (email identique)
        if (userRepository.findByEmailIgnoreCase(userDTOToCreate.getEmail()).isPresent()) {
            log.error(LogConstants.CREATE_USER_ERROR
                    + PMBExceptionConstants.ALREADY_EXIST_USER + userDTOToCreate.getEmail());
            throw new PMBException(PMBExceptionConstants.ALREADY_EXIST_USER + userDTOToCreate.getEmail());
        }

        return true;
    }
}
