package com.group30.tarecruitment.profile;

public record TaProfile(
        String taProfileId,
        String userId,
        String fullName,
        String studentId,
        String degreeProgramme,
        String gpa,
        String skills,
        String availability,
        String cvFilePath
) {
}
