package com.example.artem.task1.marketplace.mapper;

import com.example.catalogue_service.dto.UserDTO;
import com.example.catalogue_service.entity.MyUser;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    MyUser toUser(UserDTO userDTO);

    UserDTO toUserDTO(MyUser myUser);

    List<UserDTO> toUserDTOList(List<MyUser> userList);

    List<MyUser> toUserList(List<UserDTO> productDTOList);

}
