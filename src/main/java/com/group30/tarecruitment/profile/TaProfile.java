package com.group30.tarecruitment.profile;

public record TaProfile(
        String email,
        String fullName,
        String studentId,
        String contactEmail,
        String degreeProgramme,
        String gpa,
        String skills,
        String availability,
        String cvFilePath,
        String updatedAt
) {
}
