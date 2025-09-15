package com.example.common.service;

import com.example.common.exception.UserNotFoundException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@ConditionalOnClass(Jwt.class)
public final class CommonUserService {

    public static final String MY_USER_ID_KEY_KEYCLOAK = "MyUserEntityId";

    public static Long getMyUserEntityId(Jwt jwt) throws UserNotFoundException {

        UserNotFoundException exception = new UserNotFoundException();

        if(jwt==null) throw exception;

        String stringAttr = jwt.getClaimAsString(MY_USER_ID_KEY_KEYCLOAK);

        if(stringAttr==null) throw exception;

        return Long.parseLong(stringAttr);

    }

    public static String getUserKeycloakId(Jwt jwt){
        String id;
        if(jwt==null||(id=jwt.getClaim("sub"))==null) throw new IllegalArgumentException("Could not find user ID");
        return id;
    }

    public static Optional<List<String>> getUserRoles(Jwt jwt){

        if(jwt==null) return Optional.empty();


        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if(realmAccess==null) return Optional.empty();

        Object rolesObj = realmAccess.get("roles");

        if(!(rolesObj instanceof List<?> roles)) return Optional.empty();

        return Optional.of(roles)
                .map(list->list.stream()
                        .filter(role->role instanceof String)
                        .map(role->((String)role))
                        .filter(role -> role.startsWith("ROLE_"))
                        .collect(Collectors.toList())
                );
    }

    public static boolean userIsAdminOrSupportAgent(Jwt jwt){

        Optional<List<String>> optional = getUserRoles(jwt);
        if(optional.isEmpty()) return false;

        for(String role : optional.get()){
            if(role.matches("ROLE_(ADMIN|AGENT)")) return true;

        } return false;

    }


}
