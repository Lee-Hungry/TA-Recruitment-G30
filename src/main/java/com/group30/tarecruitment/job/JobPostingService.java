package com.group30.tarecruitment.job;

import java.time.OffsetDateTime;
import java.util.UUID;

public class JobPostingService {

    private static final int MIN_HOURS_PER_WEEK = 1;
    private static final int MAX_HOURS_PER_WEEK = 20;
    private final CsvJobPostingRepository repository;

    public JobPostingService(CsvJobPostingRepository repository) {
        this.repository = repository;
    }

    public JobPosting publish(JobPostingRequest request) {
        validate(request);
        JobPosting jobPosting = new JobPosting(
                "job-" + UUID.randomUUID(),
                request.createdBy(),
                request.title(),
                request.moduleCode(),
                request.description(),
                request.requiredSkills(),
                request.hoursPerWeek(),
                request.deadlineAt(),
                request.jobType(),
                "OPEN"
        );
        repository.save(jobPosting);
        return jobPosting;
    }

    private void validate(JobPostingRequest request) {
        if (isBlank(request.createdBy())
                || isBlank(request.title())
                || isBlank(request.moduleCode())
                || isBlank(request.description())
                || isBlank(request.requiredSkills())
                || isBlank(request.deadlineAt())) {
            throw new IllegalArgumentException("REQUIRED_FIELD_MISSING");
        }
        if (request.hoursPerWeek() < MIN_HOURS_PER_WEEK || request.hoursPerWeek() > MAX_HOURS_PER_WEEK) {
            throw new IllegalArgumentException("INVALID_HOURS_PER_WEEK");
        }
        OffsetDateTime deadline;
        try {
            deadline = OffsetDateTime.parse(request.deadlineAt());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("DEADLINE_FORMAT_INVALID");
        }
        if (!deadline.isAfter(OffsetDateTime.now())) {
            throw new IllegalArgumentException("DEADLINE_MUST_BE_FUTURE");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
