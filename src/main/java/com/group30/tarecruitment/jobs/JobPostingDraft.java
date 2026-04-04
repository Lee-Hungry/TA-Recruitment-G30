package com.group30.tarecruitment.jobs;

import java.time.LocalDate;

public record JobPostingDraft(
        String postedByEmail,
        String title,
        String moduleCode,
        String description,
        String requiredSkills,
        int hoursPerWeek,
        LocalDate applicationDeadline
) {
}
