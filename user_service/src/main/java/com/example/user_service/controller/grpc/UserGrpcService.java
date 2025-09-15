package com.example.user_service.controller.grpc;

import com.example.common.dto.user.UserResponseDTO;
import com.example.common.enumeration.grpc.UserExistenceStatus;
import com.example.common.exception.UserNotFoundException;
import com.example.common.grpc.user.User;
import com.example.common.grpc.user.UserServiceGrpc;
import com.example.common.mapper.grpc.UserMapper;
import com.example.user_service.service.UserKeycloakService;
import com.example.user_service.service.UserService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    private final UserKeycloakService keycloakService;

    private final UserMapper userMapper;

    public void checkUserExists(User.CheckUserRequest request,StreamObserver<User.CheckUserResponse> responseObserver) {

        UserExistenceStatus userExistenceStatus = keycloakService.userExists(request.getEmail(), request.getUsername(),request.getExcludedUser());
        User.CheckUserResponse checkUserResponse =
                User.CheckUserResponse
                        .newBuilder()
                        .setExistenceStatus(userMapper.toUserExistenceGrpcStatus(userExistenceStatus))
                        .build();

        responseObserver.onNext(checkUserResponse);
        responseObserver.onCompleted();

    }

   public void getUserInfo(User.UserRequest userRequest, StreamObserver<User.UserInfoResponse> responseObserver) {

       UserResponseDTO userResponseDTO;

       try {
           userResponseDTO = userService.collectCommonUserInfo(userRequest.getUserKeycloakId());
       } catch (UserNotFoundException e) {
           throw new RuntimeException(e);
       }

       responseObserver.onNext(userMapper.toUserInfoResponse(userResponseDTO));
       responseObserver.onCompleted();

    }


    public void checkEmailChanged(User.EmailCheckRequest request,StreamObserver<User.StatusResponse> responseObserver) {

        boolean changed = keycloakService.userEmailIsChanged(request.getUserKeycloakId(), request.getEmail());

        User.StatusResponse statusResponse = User.StatusResponse
                .newBuilder().
                setSuccess(changed)
                .build();

        responseObserver.onNext(statusResponse);
        responseObserver.onCompleted();

    }





}
