package com.group30.tarecruitment.registration;

public record TaRegistrationRequest(
        String fullName,
        String email,
        String studentId,
        String password
) {
}
