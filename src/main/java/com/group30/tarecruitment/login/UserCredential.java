package com.group30.tarecruitment.login;

public record UserCredential(
        String userId,
        String email,
        String password,
        String role,
        String status
) {
}
