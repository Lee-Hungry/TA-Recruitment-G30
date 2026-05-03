package com.group30.tarecruitment.jobs;

import java.time.LocalDate;

public record JobPostingDraft(
        String postedByEmail,
        String title,
        String moduleCode,
        String description,
        String requiredSkills,
        int hoursPerWeek,
        LocalDate applicationDeadline,
        String jobType,
        LocalDate examDate,
        String examTime,
        String location,
        Integer invigilatorsNeeded
) {

    public JobPostingDraft(
            String postedByEmail,
            String title,
            String moduleCode,
            String description,
            String requiredSkills,
            int hoursPerWeek,
            LocalDate applicationDeadline
    ) {
        this(
                postedByEmail,
                title,
                moduleCode,
                description,
                requiredSkills,
                hoursPerWeek,
                applicationDeadline,
                "TA",
                null,
                "",
                "",
                null
        );
    }

    public boolean isInvigilation() {
        return "INVIGILATION".equalsIgnoreCase(jobType);
    }
}
