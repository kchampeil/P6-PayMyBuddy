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
    public UserService(UserRepository userRepository) {
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

            createdUserDTO = Optional.ofNullable(modelMapper.map(createdUser, UserDTO.class));
            log.info(LogConstants.CREATE_USER_OK + createdUserDTO.orElse(null).getUserId());
        }

        return createdUserDTO;
    }


    /**
     * récupère les informations relatives à un utilisateur à partir de son email
     *
     * @param email de l'utilisateur dont on cherche à récupérer les informations
     * @return objet User contenant les informations de l'utilisateur concerné
     * @throws PMBException si l'email n'est pas renseigné
     */
    @Override
    public Optional<User> getUserByEmail(String email) throws PMBException {

        if (email == null || email.isEmpty()) {
            log.error(LogConstants.GET_USER_INFO_ERROR
                    + PMBExceptionConstants.MISSING_INFORMATION_GETTING_USER);
            throw new PMBException(PMBExceptionConstants.MISSING_INFORMATION_GETTING_USER);
        }

        return userRepository.findByEmailIgnoreCase(email);
    }


    /**
     * récupère les informations relatives à un utilisateur à partir de son email
     *
     * @param email de l'utilisateur dont on cherche à récupérer les informations
     * @return objet UserDTO contenant les informations de l'utilisateur concerné
     * @throws PMBException si l'email n'est pas renseigné
     *                      ou que l'utilisateur n'existe pas (non trouvé)
     */
    //TODO à revoir si utile
    @Override
    public Optional<UserDTO> getUserDTOByEmail(String email) throws PMBException {

        Optional<User> userFound = getUserByEmail(email);

        if (!userFound.isPresent()) {
            log.error(LogConstants.GET_USER_INFO_ERROR
                    + PMBExceptionConstants.DOES_NOT_EXISTS_USER + " for: " + email);
            throw new PMBException(PMBExceptionConstants.DOES_NOT_EXISTS_USER);
        }

        ModelMapper modelMapper = new ModelMapper();
        Optional<UserDTO> userDTO = Optional.ofNullable(modelMapper.map(userFound.get(), UserDTO.class));

        log.info(LogConstants.GET_USER_INFO_OK + email);

        return userDTO;
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
                    + PMBExceptionConstants.INVALID_USER_EMAIL + " for: " + userDTOToCreate.getEmail());
            throw new PMBException(PMBExceptionConstants.INVALID_USER_EMAIL);
        }

        // vérifie que l utilisateur n existe pas déjà (email identique)
        if (userRepository.findByEmailIgnoreCase(userDTOToCreate.getEmail()).isPresent()) {
            log.error(LogConstants.CREATE_USER_ERROR
                    + PMBExceptionConstants.ALREADY_EXIST_USER + " for: " + userDTOToCreate.getEmail());
            throw new PMBException(PMBExceptionConstants.ALREADY_EXIST_USER);
        }

        return true;
    }
}
