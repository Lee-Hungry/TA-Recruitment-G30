package com.group30.tarecruitment.applications;

public record MoApplicantView(
        String applicationId,
        String jobId,
        String jobTitle,
        String moduleCode,
        String fullName,
        String studentId,
        String taEmail,
        String degreeProgramme,
        String gpa,
        String skills,
        String availability,
        String cvFilePath,
        int hoursPerWeek,
        String appliedAt,
        String status
) {
}
