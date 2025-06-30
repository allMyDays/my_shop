package com.example.managerapp.mapper;


import com.example.managerapp.dto.EditUserDTO;
import com.example.managerapp.dto.RegistrationUserDTO;
import com.example.managerapp.entity.MyUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    RegistrationUserDTO toRegistrationUserDTO(MyUser user);

    EditUserDTO toEditUserDTO(MyUser user);

}
