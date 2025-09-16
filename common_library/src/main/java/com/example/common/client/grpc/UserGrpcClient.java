package com.example.common.client.grpc;

import com.example.common.dto.user.UserResponseDTO;
import com.example.common.enumeration.grpc.UserExistenceStatus;
import com.example.common.grpc.product.ProductServiceGrpc;
import com.example.common.grpc.user.User;
import com.example.common.grpc.user.UserServiceGrpc;
import com.example.common.mapper.grpc.UserMapper;
import com.netflix.appinfo.EurekaAccept;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;


@Service
@ConditionalOnClass(value = {
        ManagedChannel.class,
        ManagedChannelBuilder.class,
        EurekaAccept.class

})
@RequiredArgsConstructor
public class UserGrpcClient {

    private final UserMapper userMapper;

    private final ObjectProvider<UserServiceGrpc.UserServiceBlockingStub> userBlockingStubObjectProvider;


    public UserExistenceStatus userExists(String email, String username, String excludedUser){

        User.CheckUserRequest userRequest = User.CheckUserRequest
                .newBuilder()
                .setEmail(email)
                .setUsername(username)
                .setExcludedUser(excludedUser)
                .build();

       User.CheckUserResponse checkUserResponse =  userBlockingStubObjectProvider.getObject()
               .checkUserExists(userRequest);
       return userMapper.toUserExistenceStatus(checkUserResponse.getExistenceStatus());

    }

    public UserResponseDTO getUserInfo(String userKeycloakId){

        User.UserKeycloakIdRequest oneUserRequest = User.UserKeycloakIdRequest
                .newBuilder()
                .setUserKeycloakId(userKeycloakId)
                .build();

        User.UserInfoResponse userInfoResponse = userBlockingStubObjectProvider.getObject()
                .getUserInfo(oneUserRequest);

       return userMapper.toUserResponseDTO(userInfoResponse);

    }

    public UserResponseDTO getUserInfo2(Long userEntityId){

        User.UserEntityIdRequest oneUserRequest = User.UserEntityIdRequest
                .newBuilder()
                .setUserEntityId(userEntityId)
                .build();

        User.UserInfoResponse userInfoResponse = userBlockingStubObjectProvider.getObject()
                .getUserInfo2(oneUserRequest);

        return userMapper.toUserResponseDTO(userInfoResponse);

    }


    public boolean userEmailIsChanged(String email, String userKeycloakId){

        User.EmailCheckRequest emailCheckRequest = User.EmailCheckRequest
                .newBuilder()
                .setEmail(email)
                .setUserKeycloakId(userKeycloakId)
                .build();

        User.StatusResponse statusResponse = userBlockingStubObjectProvider.getObject()
                .checkEmailChanged(emailCheckRequest);

        return statusResponse.getSuccess();
    }



























}
