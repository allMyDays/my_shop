package com.example.user_service.init;

import com.example.common.enumeration.category.CategoryCode;
import com.example.common.enumeration.user.KeycloakRole;
import com.example.common.enumeration.user.UserExistenceStatus;
import com.example.user_service.dto.CreateUserRequestDTO;
import com.example.user_service.service.UserKeycloakService;
import com.example.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import static com.example.common.enumeration.user.KeycloakRole.ROLE_AGENT;

@Component
@RequiredArgsConstructor
@Slf4j
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

             userService.createCommonUser(createDTO, ROLE_AGENT);
             log.info("Support agent created successfully");
            }catch (Exception e){
                log.warn("Support agent creation failed: {}", e.getMessage());
            }

        } else log.info("Support agent already exists");


    }
}
