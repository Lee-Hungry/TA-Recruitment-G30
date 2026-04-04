package com.group30.tarecruitment.auth;

public record UserAccount(
        String userId,
        String email,
        String passwordHash,
        AuthRole role,
        String status,
        String createdAt,
        String updatedAt
) {
}
