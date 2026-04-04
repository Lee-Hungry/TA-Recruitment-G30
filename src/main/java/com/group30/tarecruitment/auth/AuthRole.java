package com.group30.tarecruitment.auth;

public enum AuthRole {
    TA,
    MO,
    ADMIN;

    public static AuthRole fromText(String value) {
        return AuthRole.valueOf(value.trim().toUpperCase());
    }
}
