package com.group30.tarecruitment.jobs;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class JobPostingService {

    private final CsvJobPostingRepository repository;
    private final Clock clock;

    public JobPostingService(CsvJobPostingRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    public JobPosting postJob(JobPostingDraft draft) {
        validate(draft);
        String now = OffsetDateTime.now(clock).toString();
        JobPosting posting = new JobPosting(
                "job-" + UUID.randomUUID(),
                normalizeEmail(draft.postedByEmail()),
                draft.title().trim(),
                draft.moduleCode().trim().toUpperCase(),
                draft.description().trim(),
                draft.requiredSkills().trim(),
                draft.hoursPerWeek(),
                draft.applicationDeadline(),
                "OPEN",
                now,
                now
        );
        repository.append(posting);
        return posting;
    }

    public List<JobPosting> viewPostingsByMo(String moEmail) {
        String normalizedEmail = normalizeEmail(moEmail);
        return repository.readAll().stream()
                .filter(job -> job.postedByEmail().equalsIgnoreCase(normalizedEmail))
                .sorted(Comparator.comparing(JobPosting::createdAt).reversed())
                .toList();
    }

    public List<JobPosting> browseOpenJobs() {
        return repository.readAll().stream()
                .filter(job -> "OPEN".equalsIgnoreCase(job.status()))
                .filter(job -> !job.applicationDeadline().isBefore(OffsetDateTime.now(clock).toLocalDate()))
                .sorted(Comparator.comparing(JobPosting::applicationDeadline))
                .toList();
    }

    private void validate(JobPostingDraft draft) {
        if (isBlank(draft.postedByEmail())) {
            throw new IllegalArgumentException("POSTED_BY_REQUIRED");
        }
        if (isBlank(draft.title())) {
            throw new IllegalArgumentException("TITLE_REQUIRED");
        }
        if (isBlank(draft.moduleCode())) {
            throw new IllegalArgumentException("MODULE_CODE_REQUIRED");
        }
        if (isBlank(draft.description())) {
            throw new IllegalArgumentException("DESCRIPTION_REQUIRED");
        }
        if (isBlank(draft.requiredSkills())) {
            throw new IllegalArgumentException("REQUIRED_SKILLS_REQUIRED");
        }
        if (draft.hoursPerWeek() <= 0) {
            throw new IllegalArgumentException("HOURS_INVALID");
        }
        if (draft.applicationDeadline() == null) {
            throw new IllegalArgumentException("DEADLINE_REQUIRED");
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
