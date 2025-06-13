package com.example.managerapp.record.enums;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    CLIENT, ADMIN, SELLER;


    @Override
    public String getAuthority() {
        return name();
    }
}
