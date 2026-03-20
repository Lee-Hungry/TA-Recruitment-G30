package com.group30.tarecruitment.job;

public record JobPostingRequest(
        String createdBy,
        String title,
        String moduleCode,
        String description,
        String requiredSkills,
        int hoursPerWeek,
        String deadlineAt,
        String jobType
) {
}
