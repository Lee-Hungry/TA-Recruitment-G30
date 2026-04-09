package com.group30.tarecruitment.applications;

import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPosting;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JobApplicationService {

    private final CsvJobApplicationRepository applicationRepository;
    private final CsvJobPostingRepository jobRepository;
    private final Clock clock;

    public JobApplicationService(
            CsvJobApplicationRepository applicationRepository,
            CsvJobPostingRepository jobRepository,
            Clock clock
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.clock = clock;
    }

    public JobApplication submitApplication(String taEmail, String jobId) {
        String normalizedEmail = normalizeEmail(taEmail);
        JobPosting posting = getOpenJob(jobId);

        boolean duplicateExists = applicationRepository.readAll().stream()
                .anyMatch(application -> application.jobId().equals(posting.jobId())
                        && application.taEmail().equalsIgnoreCase(normalizedEmail));
        if (duplicateExists) {
            throw new IllegalArgumentException("DUPLICATE_APPLICATION");
        }

        String now = OffsetDateTime.now(clock).toString();
        JobApplication application = new JobApplication(
                "app-" + UUID.randomUUID(),
                posting.jobId(),
                normalizedEmail,
                "PENDING",
                now,
                now
        );
        applicationRepository.append(application);
        return application;
    }

    public java.util.List<TaApplicationSummary> listApplicationsForTa(String taEmail) {
        String normalizedEmail = normalizeEmail(taEmail);
        Map<String, JobPosting> jobsById = jobRepository.readAll().stream()
                .collect(Collectors.toMap(JobPosting::jobId, Function.identity(), (left, right) -> right));

        return applicationRepository.readAll().stream()
                .filter(application -> application.taEmail().equalsIgnoreCase(normalizedEmail))
                .sorted(Comparator.comparing(JobApplication::appliedAt).reversed())
                .map(application -> {
                    JobPosting posting = jobsById.get(application.jobId());
                    if (posting == null) {
                        return new TaApplicationSummary(
                                application.applicationId(),
                                application.jobId(),
                                "Unknown Job",
                                "",
                                application.appliedAt(),
                                application.status()
                        );
                    }
                    return new TaApplicationSummary(
                            application.applicationId(),
                            application.jobId(),
                            posting.title(),
                            posting.moduleCode(),
                            application.appliedAt(),
                            application.status()
                    );
                })
                .toList();
    }

    private JobPosting getOpenJob(String jobId) {
        JobPosting posting = jobRepository.readAll().stream()
                .filter(job -> job.jobId().equals(jobId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("JOB_NOT_FOUND"));
        if (!"OPEN".equalsIgnoreCase(posting.status())) {
            throw new IllegalArgumentException("JOB_NOT_OPEN");
        }
        if (posting.applicationDeadline().isBefore(OffsetDateTime.now(clock).toLocalDate())) {
            throw new IllegalArgumentException("JOB_EXPIRED");
        }
        return posting;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
