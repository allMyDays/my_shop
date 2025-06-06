package com.example.catalogue_service.service.i;

import com.example.catalogue_service.dto.UserDTO;
import com.example.catalogue_service.entity.MyUser;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.security.Principal;

public interface UserService  extends UserDetailsService {

    boolean createUser(UserDTO userDTO);
    void saveUser(MyUser userDTO);
    boolean updateProfile(UserDTO userDTO, Principal principal);
}
