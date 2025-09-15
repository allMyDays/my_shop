package com.example.common.mapper.grpc;
import com.example.common.dto.user.UserResponseDTO;
import com.example.common.enumeration.grpc.UserExistenceStatus;
import com.example.common.grpc.user.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public abstract class UserMapper {

   public abstract User.UserInfoResponse toUserInfoResponse(UserResponseDTO userResponseDTO);

   public abstract UserResponseDTO toUserResponseDTO(User.UserInfoResponse userResponseDTO);


   @ValueMapping(source = "UNRECOGNIZED", target = MappingConstants.NULL)
   public abstract UserExistenceStatus toUserExistenceStatus(User.CheckUserResponse.ExistenceStatus grpcExistenceStatus);

   public abstract User.CheckUserResponse.ExistenceStatus toUserExistenceGrpcStatus (UserExistenceStatus userExistenceStatus);



}
