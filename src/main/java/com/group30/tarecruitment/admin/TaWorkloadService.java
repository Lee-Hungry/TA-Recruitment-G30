package com.group30.tarecruitment.admin;

import com.group30.tarecruitment.applications.CsvJobApplicationRepository;
import com.group30.tarecruitment.auth.AuthRole;
import com.group30.tarecruitment.auth.UserAccount;
import com.group30.tarecruitment.auth.repository.CsvUserAccountRepository;
import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.profile.CsvTaProfileRepository;
import com.group30.tarecruitment.profile.TaProfile;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TaWorkloadService {

    private final CsvUserAccountRepository userRepository;
    private final CsvTaProfileRepository profileRepository;
    private final CsvJobPostingRepository jobRepository;
    private final CsvJobApplicationRepository applicationRepository;
    private final WorkloadSettingsRepository settingsRepository;

    public TaWorkloadService(
            CsvUserAccountRepository userRepository,
            CsvTaProfileRepository profileRepository,
            CsvJobPostingRepository jobRepository,
            CsvJobApplicationRepository applicationRepository,
            WorkloadSettingsRepository settingsRepository
    ) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.settingsRepository = settingsRepository;
    }

    public int maxWeeklyHours() {
        return settingsRepository.maxWeeklyHours();
    }

    public List<TaWorkloadSummary> buildSummary() {
        int threshold = maxWeeklyHours();
        Map<String, TaProfile> profilesByEmail = profileRepository.readAll().stream()
                .collect(Collectors.toMap(profile -> profile.email().toLowerCase(), Function.identity(), (left, right) -> right));
        Map<String, JobPosting> jobsById = jobRepository.readAll().stream()
                .collect(Collectors.toMap(JobPosting::jobId, Function.identity(), (left, right) -> right));

        Map<String, Integer> hoursByTa = applicationRepository.readAll().stream()
                .filter(application -> "ACCEPTED".equalsIgnoreCase(application.status()))
                .filter(application -> jobsById.containsKey(application.jobId()))
                .collect(Collectors.toMap(
                        application -> application.taEmail().toLowerCase(),
                        application -> jobsById.get(application.jobId()).hoursPerWeek(),
                        Integer::sum
                ));

        Map<String, Set<String>> modulesByTa = applicationRepository.readAll().stream()
                .filter(application -> "ACCEPTED".equalsIgnoreCase(application.status()))
                .filter(application -> jobsById.containsKey(application.jobId()))
                .collect(Collectors.groupingBy(
                        application -> application.taEmail().toLowerCase(),
                        Collectors.mapping(
                                application -> jobsById.get(application.jobId()).moduleCode(),
                                Collectors.toCollection(LinkedHashSet::new)
                        )
                ));

        return userRepository.readAll().stream()
                .filter(user -> user.role() == AuthRole.TA)
                .map(user -> toSummary(user, profilesByEmail.get(user.email().toLowerCase()), hoursByTa, modulesByTa, threshold))
                .sorted(Comparator.comparingInt(TaWorkloadSummary::totalWeeklyHours).reversed()
                        .thenComparing(TaWorkloadSummary::fullName))
                .toList();
    }

    private TaWorkloadSummary toSummary(
            UserAccount user,
            TaProfile profile,
            Map<String, Integer> hoursByTa,
            Map<String, Set<String>> modulesByTa,
            int threshold
    ) {
        String email = user.email().toLowerCase();
        int totalHours = hoursByTa.getOrDefault(email, 0);
        String assignedModules = String.join(", ", modulesByTa.getOrDefault(email, Set.of()));
        String fullName = profile == null || profile.fullName().isBlank() ? email : profile.fullName();
        String studentId = profile == null ? "" : profile.studentId();
        return new TaWorkloadSummary(email, fullName, studentId, assignedModules, totalHours, totalHours > threshold);
    }
}
