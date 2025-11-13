package com.example.bankcards.util.enums;

import lombok.Getter;

@Getter
public enum RoleType {
    ADMIN("ROLE_ADMIN"), USER("ROLE_USER");

    private final String value;

    RoleType(String value) {
        this.value = value;
    }

}
