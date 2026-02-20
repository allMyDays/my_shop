package com.example.user_service.controller.rest.i;


import com.example.user_service.dto.CreateUserRequestDTO;
import com.example.user_service.dto.UpdateUserRequestDTO;
import com.example.common.dto.user.rest.UserResponseDTO;
import com.example.common.exception.UserNotFoundException;
import com.example.user_service.dto.LoginRequestDTO;
import com.example.user_service.dto.ResetPasswordDTO;
import com.example.user_service.dto.VerifyEmailRequestDTO;
import com.example.user_service.enumeration.EmailConfirmationStatus;
import com.example.user_service.enumeration.PasswordResettingStatus;
import com.example.user_service.enumeration.UserCreationStatus;
import com.example.user_service.enumeration.UserUpdateStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Map;

@Tag(name = "Пользователи", description = "API для управления пользователями, аутентификации и авторизации")
public interface IUserRestController {

    @Operation(
            summary = "Получить роли текущего пользователя",
            description = "Возвращает список ролей аутентифицированного пользователя"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Роли успешно получены"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "400", description = "Невозможно получить роли")
    })
    @SecurityRequirement(name = "JWT")
    ResponseEntity<List<String>> getUserRoles(@Parameter(hidden = true) Jwt jwt);




    @Operation(
            summary = "Создать нового пользователя",
            description = "Регистрация нового пользователя в системе. Возвращает Map<UserCreationStatus, Object>.<br><br>" +
                    "Возможные значения статусов:<br>" +
                    "• <b>ERRORS</b> - список ошибок (валидация, пользователь уже существует, пароли не совпадают)<br>" +
                    "• <b>EMAIL_SENT</b> - код подтверждения отправлен на email<br>" +
                    "• <b>SUCCESS</b> - пользователь успешно создан"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Всегда возвращает 200 OK с результатом операции",
            content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Успешное создание",
                                    value = """
                    {
                      "SUCCESS": true
                    }
                    """
                            ),
                            @ExampleObject(
                                    name = "Ошибки валидации",
                                    value = """
                    {
                      "ERRORS": ["Email должен быть валидным", "Никнейм должен содержать от 3 до 30 символов"]
                    }
                    """
                            ),
                            @ExampleObject(
                                    name = "Пользователь существует",
                                    value = """
                    {
                      "ERRORS": ["Пользователь с таким email уже существует"]
                    }
                    """
                            ),
                            @ExampleObject(
                                    name = "Отправлен код подтверждения",
                                    value = """
                    {
                      "EMAIL_SENT": true
                    }
                    """
                            )
                    }
            )
    )
    Map<UserCreationStatus, Object> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для регистрации нового пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = CreateUserRequestDTO.class))
            ) CreateUserRequestDTO userDTO,
            @Parameter(hidden = true) BindingResult bindingResult);




    @Operation(
            summary = "Сбросить пароль пользователя",
            description = "Процесс восстановления пароля. Возвращает Map<PasswordResettingStatus, Object>.<br><br>" +
                    "Возможные значения статусов:<br>" +
                    "• <b>ERRORS</b> - список ошибок<br>" +
                    "• <b>EMAIL_SENT</b> - код подтверждения отправлен на email<br>" +
                    "• <b>SUCCESS</b> - пароль успешно изменен"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Всегда возвращает 200 OK с результатом операции",
            content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Успешный сброс пароля",
                                    value = """
                    {
                      "SUCCESS": true
                    }
                    """
                            ),
                            @ExampleObject(
                                    name = "Ошибки",
                                    value = """
                    {
                      "ERRORS": ["Пользователь не найден", "Email не совпадает"]
                    }
                    """
                            ),
                            @ExampleObject(
                                    name = "Код подтверждения отправлен",
                                    value = """
                    {
                      "EMAIL_SENT": true
                    }
                    """
                            )
                    }
            )
    )
    Map<PasswordResettingStatus, Object> resetUserPassword(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для сброса пароля",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ResetPasswordDTO.class))
            ) ResetPasswordDTO passwordDTO,
            @Parameter(hidden = true) BindingResult res);




    @Operation(
            summary = "Обновить данные пользователя",
            description = "Обновление профиля аутентифицированного пользователя. Возвращает Map<UserUpdateStatus, Object>.<br><br>" +
                    "Возможные значения статусов:<br>" +
                    "• <b>ERRORS</b> - список ошибок<br>" +
                    "• <b>EMAIL_SENT</b> - код подтверждения отправлен на новый email<br>" +
                    "• <b>SUCCESS</b> - данные успешно обновлены"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Всегда возвращает 200 OK с результатом операции",
            content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Успешное обновление",
                                    value = """
                    {
                      "SUCCESS": true
                    }
                    """
                            ),
                            @ExampleObject(
                                    name = "Ошибки валидации",
                                    value = """
                    {
                      "ERRORS": ["Некорректный формат email"]
                    }
                    """
                            )
                    }
            )
    )
    @SecurityRequirement(name = "JWT")
    public Map<UserUpdateStatus, Object> updateUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Обновленные данные пользователя",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateUserRequestDTO.class))
            ) UpdateUserRequestDTO userDTO,
            @Parameter(hidden = true) BindingResult res,
            @Parameter(hidden = true) Jwt jwt);



    @Operation(
            summary = "Подтвердить email",
            description = "Верификация email по коду, отправленному на почту.<br><br>" +
                    "Возможные статусы ответа:<br>" +
                    "• <b>EXPIRED</b> - код истек или введён незнакомый приложению email адрес<br>" +
                    "• <b>NOT_MATCH</b> - код не совпадает<br>" +
                    "• <b>TOO_MANY_ATTEMPTS</b> - слишком много попыток<br>" +
                    "• <b>SUCCESS</b> - email подтвержден"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Всегда возвращает 200 OK с результатом верификации",
            content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "Успешное подтверждение",
                                    summary = "SUCCESS",
                                    value = "SUCCESS"
                            ),
                            @ExampleObject(
                                    name = "Код истек или введён незнакомый приложению email адрес",
                                    summary = "EXPIRED",
                                    value = "EXPIRED"
                            ),
                            @ExampleObject(
                                    name = "Неверный код",
                                    summary = "NOT_MATCH",
                                    value = "NOT_MATCH"
                            ),
                            @ExampleObject(
                                    name = "Слишком много попыток",
                                    summary = "TOO_MANY_ATTEMPTS",
                                    value = "TOO_MANY_ATTEMPTS"
                            )
                    }
            )
    )
    EmailConfirmationStatus verifyUserEmail(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для подтверждения email",
                    required = true,
                    content = @Content(schema = @Schema(implementation = VerifyEmailRequestDTO.class))
            ) VerifyEmailRequestDTO emailDto);






    @Operation(
            summary = "Получить информацию о пользователе",
            description = "Возвращает полную информацию о текущем аутентифицированном пользователе"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Информация о пользователе",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован"),
            @ApiResponse(responseCode = "400", description = "Ошибка при попытке собрать информацию")
    })
    @SecurityRequirement(name = "JWT")
    ResponseEntity<UserResponseDTO> getUserInfo(@Parameter(hidden = true) Jwt jwt) throws UserNotFoundException;





    @Operation(
            summary = "Вход в систему",
            description = "Аутентификация пользователя. При успешном входе устанавливается JWT cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный вход",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Login successful!"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Ошибки валидации или неверные учетные данные",
                    content = @Content(schema = @Schema(type = "array",
                            example = "[\"Не удалось войти. Проверьте правильность введенных данных.\"]"))
            )
    })
    ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Учетные данные для входа",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequestDTO.class))
            ) LoginRequestDTO loginRequestDTO,
            @Parameter(hidden = true) BindingResult bindingResult) throws UserNotFoundException;



    @Operation(
            summary = "Выход из системы",
            description = "Завершение сессии пользователя. Удаляет JWT cookie."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный выход",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "string", example = "Logout successful!"))
            ),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    @SecurityRequirement(name = "JWT")
    ResponseEntity<?> logout( @Parameter(hidden = true) Jwt jwt);



    @Operation(
            summary = "Проверить аутентификацию",
            description = "Проверяет, аутентифицирован ли текущий пользователь"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь аутентифицирован"),
            @ApiResponse(responseCode = "401", description = "Пользователь не аутентифицирован")
    })
    @SecurityRequirement(name = "JWT")
    ResponseEntity<?> isAuthenticated(@Parameter(hidden = true) Jwt jwt);

















}
