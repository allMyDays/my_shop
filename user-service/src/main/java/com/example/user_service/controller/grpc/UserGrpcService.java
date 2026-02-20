package com.example.user_service.controller.grpc;

import com.example.common.dto.user.rest.UserResponseDTO;
import com.example.common.enumeration.user.UserExistenceStatus;
import com.example.common.exception.UserNotFoundException;
import com.example.common.grpc.user.User;
import com.example.common.grpc.user.UserServiceGrpc;
import com.example.common.mapper.UserMapper;
import com.example.user_service.dto.IdAndKeycloakIdAndAvatarFileName;
import com.example.user_service.dto.UserFullNameDto;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.UserKeycloakService;
import com.example.user_service.service.UserService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    private final UserService userService;

    private final UserKeycloakService keycloakService;

    private final UserMapper userMapper;
    private final UserKeycloakService userKeycloakService;

    private final UserRepository userRepository;

    @Override
    public void checkUserExists(User.CheckUserRequest request,StreamObserver<User.CheckUserResponse> responseObserver) {

        UserExistenceStatus userExistenceStatus = keycloakService.userExists(
                request.hasEmail()?request.getEmail():null,
                request.hasUsername()?request.getUsername():null,
                request.hasExcludedUser()?request.getExcludedUser():null);
        User.CheckUserResponse checkUserResponse =
                User.CheckUserResponse
                        .newBuilder()
                        .setExistenceStatus(userMapper.toUserExistenceGrpcStatus(userExistenceStatus))
                        .build();

        responseObserver.onNext(checkUserResponse);
        responseObserver.onCompleted();

    }

    @Override
   public void getUserInfoByUserKeycloakId(User.UserKeycloakIdRequest userRequest, StreamObserver<User.UserInfoResponse> responseObserver) {

       UserResponseDTO userResponseDTO;

       try {
           userResponseDTO = userService.collectCommonUserInfo(userRequest.getUserKeycloakId());
       } catch (UserNotFoundException e) {
           throw new RuntimeException(e);
       }

       responseObserver.onNext(userMapper.toUserInfoResponse(userResponseDTO));
       responseObserver.onCompleted();

    }
    @Override
    public void getUserInfoByUserEntityId(User.UserEntityIdRequest userRequest, StreamObserver<User.UserInfoResponse> responseObserver) {

        UserResponseDTO userResponseDTO;

        try {
            userResponseDTO = userService.collectCommonUserInfo(userRequest.getUserEntityId());
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }

        responseObserver.onNext(userMapper.toUserInfoResponse(userResponseDTO));
        responseObserver.onCompleted();

    }

    @Override
    public void checkEmailChanged(User.EmailCheckRequest request,StreamObserver<User.StatusResponse> responseObserver) {

        boolean changed = keycloakService.userEmailIsChanged(request.getUserKeycloakId(), request.getEmail());

        User.StatusResponse statusResponse = User.StatusResponse
                .newBuilder().
                setSuccess(changed)
                .build();

        responseObserver.onNext(statusResponse);
        responseObserver.onCompleted();

    }


    @Override
    public void getUserMinimalInfo(User.UserEntityIdsRequest request,
                                 StreamObserver<User.UserMinimalInfoOuterResponse> responseObserver) {

        List<IdAndKeycloakIdAndAvatarFileName> userAllIds = userRepository.findByIdIn(request.getUserEntityIdsList());

        List<UserFullNameDto> userFullNameDTOs = userKeycloakService.getUserFullNames(
                userAllIds.stream()
                        .map(IdAndKeycloakIdAndAvatarFileName::getKeycloakId)
                        .toList()
        );

        Map<String, IdAndKeycloakIdAndAvatarFileName> userMap = userAllIds.stream()
                .collect(Collectors.toMap(IdAndKeycloakIdAndAvatarFileName::getKeycloakId, Function.identity()));

        List<User.UserMinimalInfoInnerResponse> reviewInfoInnerResponseList = userFullNameDTOs.stream()
                .map(dto -> {
                    IdAndKeycloakIdAndAvatarFileName temp = userMap.get(dto.getKeycloakId());
                    if (temp == null) return null;

                    String visibleName = "%s %c.".formatted(dto.getFirstName(), dto.getLastName().charAt(0));

                    var builder =
                            User.UserMinimalInfoInnerResponse.newBuilder()
                            .setUserId(temp.getId())
                            .setUserVisibleName(visibleName);
                    if(temp.getAvatarFileName()!=null){
                        builder.setAvatarFileName(temp.getAvatarFileName());
                    } return builder.build();
                })
                .filter(Objects::nonNull)
                .toList();

        responseObserver.onNext(
                User.UserMinimalInfoOuterResponse.newBuilder()
                        .addAllUserMinimalInfo(reviewInfoInnerResponseList)
                        .build()
        );
        responseObserver.onCompleted();
    }


}
