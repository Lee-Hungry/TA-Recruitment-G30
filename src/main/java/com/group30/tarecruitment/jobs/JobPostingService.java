package com.group30.tarecruitment.jobs;

import com.group30.tarecruitment.applications.CsvJobApplicationRepository;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class JobPostingService {

    private final CsvJobPostingRepository repository;
    private final CsvJobApplicationRepository applicationRepository;
    private final Clock clock;

    public JobPostingService(CsvJobPostingRepository repository, Clock clock) {
        this(repository, null, clock);
    }

    public JobPostingService(
            CsvJobPostingRepository repository,
            CsvJobApplicationRepository applicationRepository,
            Clock clock
    ) {
        this.repository = repository;
        this.applicationRepository = applicationRepository;
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
                currentOrEmptyRequiredSkills(draft),
                draft.hoursPerWeek(),
                draft.applicationDeadline(),
                "OPEN",
                now,
                now,
                normalizeJobType(draft.jobType()),
                draft.examDate(),
                cleanText(draft.examTime()),
                cleanText(draft.location()),
                draft.invigilatorsNeeded() == null ? 0 : draft.invigilatorsNeeded()
        );
        repository.append(posting);
        return posting;
    }

    public List<JobPosting> viewPostingsByMo(String moEmail) {
        return viewPostingsByOwner(moEmail);
    }

    public List<JobPosting> viewPostingsByOwner(String ownerEmail) {
        String normalizedEmail = normalizeEmail(ownerEmail);
        return repository.readAll().stream()
                .filter(job -> job.postedByEmail().equalsIgnoreCase(normalizedEmail))
                .sorted(Comparator.comparing(JobPosting::createdAt).reversed())
                .toList();
    }

    public List<JobPosting> listAllPostings() {
        return repository.readAll().stream()
                .sorted(Comparator.comparing(JobPosting::createdAt).reversed())
                .toList();
    }

    public JobPosting updatePosting(String ownerEmail, String jobId, JobPostingDraft draft) {
        validate(draft);
        JobPosting current = getOwnedJob(ownerEmail, jobId);
        String now = OffsetDateTime.now(clock).toString();
        JobPosting updated = new JobPosting(
                current.jobId(),
                current.postedByEmail(),
                cleanText(draft.title()),
                cleanText(draft.moduleCode()).toUpperCase(),
                cleanText(draft.description()),
                currentOrEmptyRequiredSkills(draft),
                draft.hoursPerWeek(),
                draft.applicationDeadline(),
                current.status(),
                current.createdAt(),
                now,
                normalizeJobType(draft.jobType()),
                draft.examDate(),
                cleanText(draft.examTime()),
                cleanText(draft.location()),
                draft.invigilatorsNeeded() == null ? 0 : draft.invigilatorsNeeded()
        );
        repository.replace(updated);
        return updated;
    }

    public void deletePosting(String ownerEmail, String jobId) {
        requireApplicationRepository();
        JobPosting posting = getOwnedJob(ownerEmail, jobId);
        boolean hasAcceptedApplications = applicationRepository.readAll().stream()
                .filter(application -> application.jobId().equals(posting.jobId()))
                .anyMatch(application -> "ACCEPTED".equalsIgnoreCase(application.status()));
        if (hasAcceptedApplications) {
            throw new IllegalArgumentException("JOB_HAS_ACCEPTED_APPLICATIONS");
        }
        repository.deleteById(posting.jobId());
        applicationRepository.deleteByJobId(posting.jobId());
    }

    public JobPosting findById(String jobId) {
        return repository.readAll().stream()
                .filter(job -> job.jobId().equals(jobId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("JOB_NOT_FOUND"));
    }

    public List<JobPosting> browseOpenJobs() {
        return browseOpenJobs("", "", "");
    }

    public List<JobPosting> browseOpenJobs(String keyword, String requiredSkill, String moduleCode) {
        String normalizedKeyword = normalizeSearch(keyword);
        String normalizedSkill = normalizeSearch(requiredSkill);
        String normalizedModule = normalizeSearch(moduleCode);
        return repository.readAll().stream()
                .filter(job -> "OPEN".equalsIgnoreCase(job.status()))
                .filter(job -> !job.applicationDeadline().isBefore(OffsetDateTime.now(clock).toLocalDate()))
                .filter(job -> normalizedKeyword.isBlank()
                        || containsIgnoreCase(job.title(), normalizedKeyword)
                        || containsIgnoreCase(job.moduleCode(), normalizedKeyword))
                .filter(job -> normalizedSkill.isBlank()
                        || splitTokens(job.requiredSkills()).stream().anyMatch(skill -> skill.equalsIgnoreCase(normalizedSkill)))
                .filter(job -> normalizedModule.isBlank()
                        || job.moduleCode().equalsIgnoreCase(normalizedModule))
                .sorted(Comparator.comparing(JobPosting::applicationDeadline))
                .toList();
    }

    public List<String> listOpenJobSkills() {
        return browseOpenJobs().stream()
                .flatMap(job -> splitTokens(job.requiredSkills()).stream())
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .toList();
    }

    public List<String> listOpenJobModules() {
        return browseOpenJobs().stream()
                .map(JobPosting::moduleCode)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public List<JobPosting> browseOpenInvigilationJobs() {
        return repository.readAll().stream()
                .filter(JobPosting::isInvigilation)
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
        if (!draft.isInvigilation() && isBlank(draft.requiredSkills())) {
            throw new IllegalArgumentException("REQUIRED_SKILLS_REQUIRED");
        }
        if (draft.hoursPerWeek() <= 0) {
            throw new IllegalArgumentException("HOURS_INVALID");
        }
        if (draft.applicationDeadline() == null) {
            throw new IllegalArgumentException("DEADLINE_REQUIRED");
        }
        String normalizedJobType = normalizeJobType(draft.jobType());
        if (!"TA".equals(normalizedJobType) && !"INVIGILATION".equals(normalizedJobType)) {
            throw new IllegalArgumentException("JOB_TYPE_INVALID");
        }
        if ("INVIGILATION".equals(normalizedJobType)) {
            if (draft.examDate() == null) {
                throw new IllegalArgumentException("EXAM_DATE_REQUIRED");
            }
            if (isBlank(draft.examTime())) {
                throw new IllegalArgumentException("EXAM_TIME_REQUIRED");
            }
            if (isBlank(draft.location())) {
                throw new IllegalArgumentException("LOCATION_REQUIRED");
            }
            if (draft.invigilatorsNeeded() == null || draft.invigilatorsNeeded() <= 0) {
                throw new IllegalArgumentException("INVIGILATORS_NEEDED_INVALID");
            }
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeJobType(String jobType) {
        String normalized = jobType == null ? "" : jobType.trim().toUpperCase();
        return normalized.isBlank() ? "TA" : normalized;
    }

    private String normalizeSearch(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean containsIgnoreCase(String source, String needle) {
        return source != null && source.toLowerCase().contains(needle);
    }

    private List<String> splitTokens(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Stream.of(value.split(","))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }

    private String currentOrEmptyRequiredSkills(JobPostingDraft draft) {
        return draft.requiredSkills() == null ? "" : draft.requiredSkills().trim();
    }

    private String cleanText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private JobPosting getOwnedJob(String ownerEmail, String jobId) {
        String normalizedEmail = normalizeEmail(ownerEmail);
        return repository.readAll().stream()
                .filter(job -> job.jobId().equals(jobId))
                .filter(job -> job.postedByEmail().equalsIgnoreCase(normalizedEmail))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("JOB_NOT_FOUND_FOR_OWNER"));
    }

    private void requireApplicationRepository() {
        if (applicationRepository == null) {
            throw new IllegalStateException("APPLICATION_REPOSITORY_REQUIRED");
        }
    }
}
