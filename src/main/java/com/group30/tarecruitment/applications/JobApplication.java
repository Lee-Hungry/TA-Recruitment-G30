package com.group30.tarecruitment.applications;

public record JobApplication(
        String applicationId,
        String jobId,
        String taEmail,
        String status,
        String appliedAt,
        String updatedAt
) {

    public JobApplication withStatus(String nextStatus, String nextUpdatedAt) {
        return new JobApplication(applicationId, jobId, taEmail, nextStatus, appliedAt, nextUpdatedAt);
    }

    public boolean isWithdrawn() {
        return "WITHDRAWN".equalsIgnoreCase(status);
    }
}
