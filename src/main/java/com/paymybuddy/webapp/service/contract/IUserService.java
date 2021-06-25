package com.paymybuddy.webapp.service.contract;

import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import com.paymybuddy.webapp.model.User;

import java.util.Optional;

public interface IUserService {

    Optional<UserDTO> createUser(UserDTO userDTOToCreate) throws PMBException;

    Optional<User> getUserByEmail(String email) throws PMBException;

    Optional<UserDTO> getUserDTOByEmail(String email) throws PMBException;
}
