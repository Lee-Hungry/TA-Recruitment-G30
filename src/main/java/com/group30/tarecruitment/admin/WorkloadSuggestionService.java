package com.group30.tarecruitment.admin;

import com.group30.tarecruitment.ai.AiSettingsRepository;
import com.group30.tarecruitment.ai.OpenAiJsonClient;
import com.group30.tarecruitment.ai.OpenAiRecruitmentGateway;
import com.group30.tarecruitment.ai.RecruitmentAiGateway;
import com.group30.tarecruitment.applications.CsvJobApplicationRepository;
import com.group30.tarecruitment.applications.JobApplication;
import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.matching.SkillMatchResult;
import com.group30.tarecruitment.matching.SkillMatchingService;
import com.group30.tarecruitment.profile.CsvTaProfileRepository;
import com.group30.tarecruitment.profile.TaProfile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class WorkloadSuggestionService {

    private final TaWorkloadService workloadService;
    private final CsvJobPostingRepository jobRepository;
    private final CsvJobApplicationRepository applicationRepository;
    private final CsvTaProfileRepository profileRepository;
    private final SkillMatchingService skillMatchingService;
    private final RecruitmentAiGateway aiGateway;

    public WorkloadSuggestionService(
            TaWorkloadService workloadService,
            CsvJobPostingRepository jobRepository,
            CsvJobApplicationRepository applicationRepository,
            CsvTaProfileRepository profileRepository,
            SkillMatchingService skillMatchingService
    ) {
        this(
                workloadService,
                jobRepository,
                applicationRepository,
                profileRepository,
                skillMatchingService,
                new OpenAiRecruitmentGateway(
                        new OpenAiJsonClient(new AiSettingsRepository(Path.of("data", "ai.properties")).load())
                )
        );
    }

    public WorkloadSuggestionService(
            TaWorkloadService workloadService,
            CsvJobPostingRepository jobRepository,
            CsvJobApplicationRepository applicationRepository,
            CsvTaProfileRepository profileRepository,
            SkillMatchingService skillMatchingService,
            RecruitmentAiGateway aiGateway
    ) {
        this.workloadService = workloadService;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.profileRepository = profileRepository;
        this.skillMatchingService = skillMatchingService;
        this.aiGateway = aiGateway;
    }

    public List<WorkloadSuggestion> buildSuggestions() {
        List<TaWorkloadSummary> overloadedRows = workloadService.buildSummary().stream()
                .filter(TaWorkloadSummary::overloaded)
                .toList();
        if (overloadedRows.isEmpty()) {
            return List.of(new WorkloadSuggestion(
                    "Workload is balanced",
                    "No TA is above the configured weekly-hour limit right now. Continue monitoring new acceptances.",
                    false,
                    "",
                    "",
                    ""
            ));
        }

        Map<String, JobPosting> jobsById = jobRepository.readAll().stream()
                .collect(Collectors.toMap(JobPosting::jobId, Function.identity(), (left, right) -> right));
        Map<String, TaProfile> profilesByEmail = profileRepository.readAll().stream()
                .collect(Collectors.toMap(profile -> profile.email().toLowerCase(), Function.identity(), (left, right) -> right));
        Map<String, Integer> hoursByEmail = workloadService.buildSummary().stream()
                .collect(Collectors.toMap(summary -> summary.taEmail().toLowerCase(), TaWorkloadSummary::totalWeeklyHours));

        List<JobApplication> applications = applicationRepository.readAll();
        List<WorkloadSuggestion> suggestions = new ArrayList<>();
        for (TaWorkloadSummary overloaded : overloadedRows) {
            suggestions.add(buildSuggestionForTa(overloaded, applications, jobsById, profilesByEmail, hoursByEmail));
        }
        return suggestions;
    }

    private WorkloadSuggestion buildSuggestionForTa(
            TaWorkloadSummary overloaded,
            List<JobApplication> applications,
            Map<String, JobPosting> jobsById,
            Map<String, TaProfile> profilesByEmail,
            Map<String, Integer> hoursByEmail
    ) {
        List<JobApplication> acceptedAssignments = applications.stream()
                .filter(application -> "ACCEPTED".equalsIgnoreCase(application.status()))
                .filter(application -> application.taEmail().equalsIgnoreCase(overloaded.taEmail()))
                .filter(application -> jobsById.containsKey(application.jobId()))
                .sorted(Comparator.comparingInt((JobApplication application) -> jobsById.get(application.jobId()).hoursPerWeek()).reversed())
                .toList();

        List<JobPosting> acceptedJobs = acceptedAssignments.stream()
                .map(assignment -> jobsById.get(assignment.jobId()))
                .toList();
        Map<String, List<WorkloadCandidateOption>> candidatesByJobId = new LinkedHashMap<>();
        for (JobPosting acceptedJob : acceptedJobs) {
            candidatesByJobId.put(
                    acceptedJob.jobId(),
                    findReplacementCandidates(acceptedJob, applications, profilesByEmail, hoursByEmail)
            );
        }

        try {
            return aiGateway.suggestWorkloadAdjustment(
                    overloaded,
                    workloadService.maxWeeklyHours(),
                    acceptedJobs,
                    candidatesByJobId
            );
        } catch (Exception ignored) {
        }

        return fallbackSuggestion(overloaded, acceptedJobs, candidatesByJobId);
    }

    private List<WorkloadCandidateOption> findReplacementCandidates(
            JobPosting job,
            List<JobApplication> applications,
            Map<String, TaProfile> profilesByEmail,
            Map<String, Integer> hoursByEmail
    ) {
        return applications.stream()
                .filter(application -> application.jobId().equals(job.jobId()))
                .filter(application -> "PENDING".equalsIgnoreCase(application.status()))
                .filter(application -> hoursByEmail.getOrDefault(application.taEmail().toLowerCase(), 0) + job.hoursPerWeek()
                        <= workloadService.maxWeeklyHours())
                .map(application -> toCandidateOption(application, job, profilesByEmail, hoursByEmail))
                .sorted(Comparator.comparingInt((WorkloadCandidateOption option) -> option.matchResult().matchScore()).reversed())
                .limit(3)
                .toList();
    }

    private WorkloadCandidateOption toCandidateOption(
            JobApplication application,
            JobPosting job,
            Map<String, TaProfile> profilesByEmail,
            Map<String, Integer> hoursByEmail
    ) {
        TaProfile profile = profilesByEmail.get(application.taEmail().toLowerCase());
        SkillMatchResult matchResult = profile == null
                ? skillMatchingService.analyze("", job.requiredSkills())
                : skillMatchingService.analyze(profile, job);
        int currentHours = hoursByEmail.getOrDefault(application.taEmail().toLowerCase(), 0);
        int projectedHours = currentHours + job.hoursPerWeek();
        return new WorkloadCandidateOption(
                application.taEmail(),
                profile == null || profile.fullName().isBlank() ? application.taEmail() : profile.fullName(),
                currentHours,
                projectedHours,
                matchResult
        );
    }

    private WorkloadSuggestion fallbackSuggestion(
            TaWorkloadSummary overloaded,
            List<JobPosting> acceptedJobs,
            Map<String, List<WorkloadCandidateOption>> candidatesByJobId
    ) {
        for (JobPosting acceptedJob : acceptedJobs) {
            List<WorkloadCandidateOption> candidates = candidatesByJobId.getOrDefault(acceptedJob.jobId(), List.of());
            if (!candidates.isEmpty()) {
                WorkloadCandidateOption candidate = candidates.getFirst();
                return new WorkloadSuggestion(
                        "Review " + candidate.candidateName() + " for " + acceptedJob.moduleCode(),
                        overloaded.fullName() + " is above the workload limit. " + candidate.candidateName()
                                + " has an AI match score of " + candidate.matchResult().matchScore()
                                + "% for " + acceptedJob.title() + " and would remain at "
                                + candidate.projectedHours() + " hrs/week if selected.",
                        true,
                        overloaded.taEmail(),
                        candidate.candidateEmail(),
                        acceptedJob.jobId()
                );
            }
        }

        return new WorkloadSuggestion(
                overloaded.fullName() + " is overloaded",
                overloaded.fullName() + " is assigned " + overloaded.totalWeeklyHours()
                        + " hrs/week. No suitable pending replacement was found, so consider splitting hours or opening a new supporting posting.",
                false,
                overloaded.taEmail(),
                "",
                ""
        );
    }
}
