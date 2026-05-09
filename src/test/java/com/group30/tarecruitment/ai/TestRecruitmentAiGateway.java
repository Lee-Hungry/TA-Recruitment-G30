package com.group30.tarecruitment.ai;

import com.group30.tarecruitment.admin.TaWorkloadSummary;
import com.group30.tarecruitment.admin.WorkloadCandidateOption;
import com.group30.tarecruitment.admin.WorkloadSuggestion;
import com.group30.tarecruitment.applications.MoApplicantView;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.matching.SkillMatchResult;
import com.group30.tarecruitment.profile.TaProfile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TestRecruitmentAiGateway implements RecruitmentAiGateway {

    @Override
    public SkillMatchResult analyzeSkillMatch(TaProfile profile, JobPosting posting) {
        return analyze(profile.skills(), posting.requiredSkills());
    }

    @Override
    public Map<String, SkillMatchResult> analyzeApplicants(JobPosting posting, List<MoApplicantView> applicants) {
        Map<String, SkillMatchResult> results = new LinkedHashMap<>();
        for (MoApplicantView applicant : applicants) {
            results.put(applicant.applicationId(), analyze(applicant.skills(), posting.requiredSkills()));
        }
        return results;
    }

    @Override
    public WorkloadSuggestion suggestWorkloadAdjustment(
            TaWorkloadSummary overloaded,
            int threshold,
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
                                + " would remain at " + candidate.projectedHours() + " hrs/week if selected.",
                        true,
                        overloaded.taEmail(),
                        candidate.candidateEmail(),
                        acceptedJob.jobId()
                );
            }
        }
        return new WorkloadSuggestion(
                overloaded.fullName() + " is overloaded",
                "No suitable pending candidate was found under the workload threshold.",
                false,
                overloaded.taEmail(),
                "",
                ""
        );
    }

    private SkillMatchResult analyze(String profileSkills, String requiredSkills) {
        Map<String, String> profileTokens = tokenize(profileSkills);
        Map<String, String> requiredTokens = tokenize(requiredSkills);
        if (requiredTokens.isEmpty()) {
            return new SkillMatchResult(100, List.of(), List.of(), "No required skills were listed.");
        }

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, String> required : requiredTokens.entrySet()) {
            if (profileTokens.containsKey(required.getKey())) {
                matched.add(required.getValue());
            } else {
                missing.add(required.getValue());
            }
        }
        int matchScore = (int) Math.round(matched.size() * 100.0 / requiredTokens.size());
        return new SkillMatchResult(matchScore, matched, missing, "Test AI recommendation.");
    }

    private Map<String, String> tokenize(String rawSkills) {
        Map<String, String> tokens = new LinkedHashMap<>();
        if (rawSkills == null || rawSkills.isBlank()) {
            return tokens;
        }
        Set<String> parts = new LinkedHashSet<>();
        for (String token : rawSkills.split("[,;\\n\\r/|]+")) {
            String display = token.trim();
            String normalized = display.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9#+]+", "");
            if (!normalized.isBlank()) {
                parts.add(normalized);
                tokens.putIfAbsent(normalized, display);
            }
        }
        return tokens;
    }
}
