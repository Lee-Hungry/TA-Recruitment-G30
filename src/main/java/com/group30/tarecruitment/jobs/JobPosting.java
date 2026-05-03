package com.group30.tarecruitment.jobs;

import java.time.LocalDate;

public record JobPosting(
        String jobId,
        String postedByEmail,
        String title,
        String moduleCode,
        String description,
        String requiredSkills,
        int hoursPerWeek,
        LocalDate applicationDeadline,
        String status,
        String createdAt,
        String updatedAt,
        String jobType,
        LocalDate examDate,
        String examTime,
        String location,
        int invigilatorsNeeded
) {

    public JobPosting(
            String jobId,
            String postedByEmail,
            String title,
            String moduleCode,
            String description,
            String requiredSkills,
            int hoursPerWeek,
            LocalDate applicationDeadline,
            String status,
            String createdAt,
            String updatedAt
    ) {
        this(
                jobId,
                postedByEmail,
                title,
                moduleCode,
                description,
                requiredSkills,
                hoursPerWeek,
                applicationDeadline,
                status,
                createdAt,
                updatedAt,
                "TA",
                null,
                "",
                "",
                0
        );
    }

    public boolean isInvigilation() {
        return "INVIGILATION".equalsIgnoreCase(jobType);
    }
}
