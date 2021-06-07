package com.paymybuddy.webapp.service;

import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.UserDTO;

import java.util.Optional;

public interface IUserService {

    Optional<UserDTO> createUser(UserDTO userDTOToCreate) throws PMBException;

}
