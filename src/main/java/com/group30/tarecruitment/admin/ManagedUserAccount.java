package com.group30.tarecruitment.admin;

public record ManagedUserAccount(
        String userId,
        String email,
        String displayName,
        String role,
        String status,
        String studentId,
        String updatedAt
) {
}
