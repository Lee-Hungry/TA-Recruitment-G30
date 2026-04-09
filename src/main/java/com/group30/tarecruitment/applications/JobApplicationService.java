package com.group30.tarecruitment.applications;

import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.profile.CsvTaProfileRepository;
import com.group30.tarecruitment.profile.TaProfile;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class JobApplicationService {

    private final CsvJobApplicationRepository applicationRepository;
    private final CsvJobPostingRepository jobRepository;
    private final CsvTaProfileRepository profileRepository;
    private final Clock clock;

    public JobApplicationService(
            CsvJobApplicationRepository applicationRepository,
            CsvJobPostingRepository jobRepository,
            CsvTaProfileRepository profileRepository,
            Clock clock
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.profileRepository = profileRepository;
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

    public List<TaApplicationSummary> listApplicationsForTa(String taEmail) {
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

    public List<MoApplicantView> listApplicantsForJob(String moEmail, String jobId) {
        JobPosting posting = getOwnedJob(moEmail, jobId);
        return applicationRepository.readAll().stream()
                .filter(application -> application.jobId().equals(posting.jobId()))
                .sorted(Comparator.comparing(JobApplication::appliedAt))
                .map(application -> toMoApplicantView(posting, application))
                .toList();
    }

    public JobApplication updateApplicationStatus(String moEmail, String applicationId, String nextStatus) {
        String normalizedStatus = normalizeStatus(nextStatus);
        if (!"ACCEPTED".equals(normalizedStatus) && !"REJECTED".equals(normalizedStatus)) {
            throw new IllegalArgumentException("INVALID_APPLICATION_STATUS");
        }

        JobApplication current = applicationRepository.readAll().stream()
                .filter(application -> application.applicationId().equals(applicationId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("APPLICATION_NOT_FOUND"));
        JobPosting posting = getOwnedJob(moEmail, current.jobId());
        if (posting.applicationDeadline().isBefore(OffsetDateTime.now(clock).toLocalDate())) {
            throw new IllegalArgumentException("DECISION_DEADLINE_PASSED");
        }

        JobApplication updated = current.withStatus(normalizedStatus, OffsetDateTime.now(clock).toString());
        applicationRepository.replace(updated);
        return updated;
    }

    private MoApplicantView toMoApplicantView(JobPosting posting, JobApplication application) {
        TaProfile profile = profileRepository.findByEmail(application.taEmail()).orElseGet(() -> new TaProfile(
                application.taEmail(),
                application.taEmail(),
                "",
                application.taEmail(),
                "",
                "",
                "",
                "",
                "",
                ""
        ));

        return new MoApplicantView(
                application.applicationId(),
                posting.jobId(),
                posting.title(),
                posting.moduleCode(),
                profile.fullName(),
                profile.studentId(),
                application.taEmail(),
                profile.degreeProgramme(),
                profile.gpa(),
                profile.skills(),
                profile.availability(),
                profile.cvFilePath(),
                posting.hoursPerWeek(),
                application.appliedAt(),
                application.status()
        );
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

    private JobPosting getOwnedJob(String moEmail, String jobId) {
        String normalizedEmail = normalizeEmail(moEmail);
        return jobRepository.readAll().stream()
                .filter(job -> job.jobId().equals(jobId))
                .filter(job -> job.postedByEmail().equalsIgnoreCase(normalizedEmail))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("JOB_NOT_FOUND_FOR_MO"));
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase();
    }
}
