package com.example.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.common.service.CommonUserService.getUserRoles;

@Component
@ConditionalOnClass(value =
        {jakarta.servlet.FilterChain.class,
        jakarta.servlet.ServletException.class,
        jakarta.servlet.http.HttpServletRequest.class,
        jakarta.servlet.http.HttpServletResponse.class,
        org.springframework.security.authentication.UsernamePasswordAuthenticationToken.class,
        org.springframework.security.core.GrantedAuthority.class,
        org.springframework.security.core.authority.SimpleGrantedAuthority.class,
        org.springframework.security.core.context.SecurityContext.class,
        org.springframework.security.core.context.SecurityContextHolder.class,
        org.springframework.security.oauth2.jwt.Jwt.class,
                org.springframework.web.filter.OncePerRequestFilter.class
        })
@ConditionalOnProperty(name = "spring.security.oauth2.resourceserver.jwt.issuer-uri")
public class JwtRolesPostProcessingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        SecurityContext context = SecurityContextHolder.getContext();
        if (context!=null&&context.getAuthentication() != null&&context.getAuthentication().getPrincipal() instanceof Jwt jwt) {

            Optional<List<String>> rolesOpt =  getUserRoles(jwt);

            if (rolesOpt.isPresent()) {
                List<GrantedAuthority> authorities = rolesOpt.get().stream()
                        .filter(role -> role.startsWith("ROLE_"))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(jwt, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }
}
