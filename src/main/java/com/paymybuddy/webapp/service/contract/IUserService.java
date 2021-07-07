package com.paymybuddy.webapp.service.contract;

import com.paymybuddy.webapp.exception.PMBException;
import com.paymybuddy.webapp.model.DTO.UserDTO;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

public interface IUserService {

    Optional<UserDTO> createUser(UserDTO userDTOToCreate) throws PMBException;

    UserDTO getUserDTOByEmail(String email) throws UsernameNotFoundException;

}
