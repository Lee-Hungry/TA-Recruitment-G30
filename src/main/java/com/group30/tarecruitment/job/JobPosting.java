package com.group30.tarecruitment.job;

public record JobPosting(
        String jobId,
        String createdBy,
        String title,
        String moduleCode,
        String description,
        String requiredSkills,
        int hoursPerWeek,
        String deadlineAt,
        String jobType,
        String status
) {
}
