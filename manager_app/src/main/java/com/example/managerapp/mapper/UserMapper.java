package com.example.managerapp.mapper;


import com.example.managerapp.dto.user.UserRegistrationDTO;
import com.example.managerapp.dto.user.UserEditProfileDTO;
import com.example.managerapp.entity.MyUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserRegistrationDTO toRegistrationUserDTO(MyUser user);

    UserEditProfileDTO toEditUserDTO(MyUser user);

}
