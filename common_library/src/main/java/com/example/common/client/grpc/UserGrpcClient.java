package com.example.common.client.grpc;

import com.example.common.dto.user.rest.UserMinimalInfoDto;
import com.example.common.dto.user.rest.UserResponseDTO;
import com.example.common.enumeration.user.UserExistenceStatus;
import com.example.common.grpc.user.User;
import com.example.common.grpc.user.UserServiceGrpc;
import com.example.common.mapper.AddressMapper;
import com.example.common.mapper.UserMapper;
import com.netflix.appinfo.EurekaAccept;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@ConditionalOnClass(value = {
        ManagedChannel.class,
        ManagedChannelBuilder.class,
        EurekaAccept.class

})
@RequiredArgsConstructor
public class UserGrpcClient {

    private final UserMapper userMapper;

    private final AddressMapper addressMapper;

    private final ObjectProvider<UserServiceGrpc.UserServiceBlockingStub> userBlockingStubObjectProvider;


    public UserExistenceStatus userExists(Optional<String> email, Optional<String> username, Optional<String> excludedUser) {


        User.CheckUserRequest.Builder userRequestBuilder = User.CheckUserRequest.newBuilder();

        email.ifPresent(userRequestBuilder::setEmail);
        username.ifPresent(userRequestBuilder::setUsername);
        excludedUser.ifPresent(userRequestBuilder::setExcludedUser);

        User.CheckUserResponse checkUserResponse = userBlockingStubObjectProvider.getObject()
                .checkUserExists(userRequestBuilder.build());
        return userMapper.toUserExistenceStatus(checkUserResponse.getExistenceStatus());

    }

    public UserResponseDTO getUserInfoByKeycloakId(@NonNull String userKeycloakId) {

        User.UserKeycloakIdRequest oneUserRequest = User.UserKeycloakIdRequest
                .newBuilder()
                .setUserKeycloakId(userKeycloakId)
                .build();

        User.UserInfoResponse userInfoResponse = userBlockingStubObjectProvider.getObject()
                .getUserInfoByUserKeycloakId(oneUserRequest);

        return userMapper.toUserResponseDTO(userInfoResponse);

    }

    public UserResponseDTO getUserInfoByEntityId(long userEntityId) {

        User.UserEntityIdRequest oneUserRequest = User.UserEntityIdRequest
                .newBuilder()
                .setUserEntityId(userEntityId)
                .build();

        User.UserInfoResponse userInfoResponse = userBlockingStubObjectProvider.getObject()
                .getUserInfoByUserEntityId(oneUserRequest);

        return userMapper.toUserResponseDTO(userInfoResponse);

    }


    public boolean userEmailIsChanged(@NonNull String email, @NonNull String userKeycloakId) {

        User.EmailCheckRequest emailCheckRequest = User.EmailCheckRequest
                .newBuilder()
                .setEmail(email)
                .setUserKeycloakId(userKeycloakId)
                .build();

        User.StatusResponse statusResponse = userBlockingStubObjectProvider.getObject()
                .checkEmailChanged(emailCheckRequest);

        return statusResponse.getSuccess();
    }

    public List<UserMinimalInfoDto> getUserMinimalInfo(@NonNull List<Long> userEntityIds) {

        User.UserEntityIdsRequest userEntityIdsRequest = User.UserEntityIdsRequest
                .newBuilder()
                .addAllUserEntityIds(userEntityIds)
                .build();

        User.UserMinimalInfoOuterResponse response = userBlockingStubObjectProvider.getObject()
                .getUserMinimalInfo(userEntityIdsRequest);

        return userMapper.toUserMinimalInfoDTOs(response.getUserMinimalInfoList());
    }
}























