package com.group30.tarecruitment.job;

import java.util.UUID;

public class JobPostingService {

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
        if (isBlank(request.createdBy()) || isBlank(request.title()) || isBlank(request.moduleCode()) || isBlank(request.deadlineAt())) {
            throw new IllegalArgumentException("REQUIRED_FIELD_MISSING");
        }
        if (request.hoursPerWeek() <= 0) {
            throw new IllegalArgumentException("INVALID_HOURS_PER_WEEK");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
