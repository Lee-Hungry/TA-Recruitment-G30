package com.group30.tarecruitment.applications;

public record TaApplicationSummary(
        String applicationId,
        String jobId,
        String jobTitle,
        String moduleCode,
        String appliedAt,
        String status
) {
}
