package com.example.user_service.init;

import com.example.common.enumeration.user.UserExistenceStatus;
import com.example.user_service.dto.CreateUserRequestDTO;
import com.example.user_service.enumeration.UserCreationInnerStatus;
import com.example.user_service.service.UserKeycloakService;
import com.example.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.example.common.enumeration.user.KeycloakRole.ROLE_AGENT;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("test")
public class DataInitializer implements ApplicationRunner {

    private final UserService userService;

    private final UserKeycloakService userKeycloakService;


    @Override
    public void run(ApplicationArguments args) throws Exception {

        if(userKeycloakService.userExists(null,"агент", null).equals(UserExistenceStatus.NOT_EXISTS)){
            try{
             CreateUserRequestDTO createDTO = new CreateUserRequestDTO(
                    "агент",
                    "Агент",
                    "Поддержки",
                    "agent@mail.ru",
                    "agent",
                    "agent");

                UserCreationInnerStatus status = userService.createCommonUser(createDTO, ROLE_AGENT);
                if(status.equals(UserCreationInnerStatus.SUCCESS)){
                    log.info("Support agent created successfully");
                } else log.info("Support agent creation failed");
            }catch (Exception e){
                log.warn("Support agent creation failed: {}", e.getMessage());
            }

        } else log.info("Support agent already exists");


    }
}
