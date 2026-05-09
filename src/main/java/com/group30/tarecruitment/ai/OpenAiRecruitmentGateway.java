package com.group30.tarecruitment.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.group30.tarecruitment.admin.TaWorkloadSummary;
import com.group30.tarecruitment.admin.WorkloadCandidateOption;
import com.group30.tarecruitment.admin.WorkloadSuggestion;
import com.group30.tarecruitment.applications.MoApplicantView;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.matching.SkillMatchResult;
import com.group30.tarecruitment.profile.TaProfile;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OpenAiRecruitmentGateway implements RecruitmentAiGateway {

    private static final String MATCH_SYSTEM_PROMPT = """
            You are an academic recruitment assistant.
            Evaluate fit between a TA profile and a TA-related job posting.
            Return JSON only with keys: matchScore, matchedSkills, missingSkills, recommendation.
            Requirements:
            - matchScore must be an integer from 0 to 100.
            - matchedSkills and missingSkills must be arrays of short strings.
            - recommendation must be a concise recommendation, not a final decision.
            - Use semantic understanding, not just exact keyword overlap.
            - Be evidence-based: only count a skill as matched if the profile reasonably supports it.
            """;

    private static final String APPLICANT_BATCH_SYSTEM_PROMPT = """
            You are an academic recruitment assistant helping a module organiser compare applicants.
            Return JSON only with this shape:
            {
              "results": [
                {
                  "applicationId": "...",
                  "matchScore": 0,
                  "matchedSkills": ["..."],
                  "missingSkills": ["..."],
                  "recommendation": "..."
                }
              ]
            }
            Requirements:
            - Use semantic understanding of skills and related experience.
            - Keep matchScore as integer 0-100.
            - Keep recommendation concise and explainable.
            - Include one result for every applicant provided.
            """;

    private static final String WORKLOAD_SYSTEM_PROMPT = """
            You are an admin assistant for TA workload balancing.
            Return JSON only with keys:
            title, detail, actionable, overloadedTaEmail, suggestedCandidateEmail, suggestedJobId.
            Requirements:
            - Suggest manual actions only. Never imply automatic reassignment.
            - actionable must be true only when there is a concrete candidate and job to review.
            - detail must explain why the candidate is a good manual review option or why no suitable candidate exists.
            - Keep detail concise, practical, and grounded in the provided workload and skill evidence.
            """;

    private final OpenAiJsonClient client;

    public OpenAiRecruitmentGateway(OpenAiJsonClient client) {
        this.client = client;
    }

    @Override
    public SkillMatchResult analyzeSkillMatch(TaProfile profile, JobPosting posting) {
        JsonNode root = client.requestJsonObject(MATCH_SYSTEM_PROMPT, buildMatchPrompt(profile, posting));
        return toSkillMatchResult(root);
    }

    @Override
    public Map<String, SkillMatchResult> analyzeApplicants(JobPosting posting, List<MoApplicantView> applicants) {
        JsonNode root = client.requestJsonObject(APPLICANT_BATCH_SYSTEM_PROMPT, buildApplicantBatchPrompt(posting, applicants));
        Map<String, SkillMatchResult> results = new LinkedHashMap<>();
        for (JsonNode item : root.path("results")) {
            String applicationId = item.path("applicationId").asText("");
            if (!applicationId.isBlank()) {
                results.put(applicationId, toSkillMatchResult(item));
            }
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
        JsonNode root = client.requestJsonObject(
                WORKLOAD_SYSTEM_PROMPT,
                buildWorkloadPrompt(overloaded, threshold, acceptedJobs, candidatesByJobId)
        );
        return new WorkloadSuggestion(
                text(root, "title", overloaded.fullName() + " workload suggestion"),
                text(root, "detail", overloaded.fullName() + " requires manual workload review."),
                root.path("actionable").asBoolean(false),
                text(root, "overloadedTaEmail", overloaded.taEmail()),
                text(root, "suggestedCandidateEmail", ""),
                text(root, "suggestedJobId", "")
        );
    }

    private String buildMatchPrompt(TaProfile profile, JobPosting posting) {
        return """
                Candidate profile
                Name: %s
                Degree: %s
                GPA: %s
                Skills: %s
                Availability: %s

                Job posting
                Title: %s
                Module: %s
                Type: %s
                Description: %s
                Required skills: %s
                Hours per week: %d
                """.formatted(
                safe(profile.fullName()),
                safe(profile.degreeProgramme()),
                safe(profile.gpa()),
                safe(profile.skills()),
                safe(profile.availability()),
                safe(posting.title()),
                safe(posting.moduleCode()),
                posting.isInvigilation() ? "INVIGILATION" : "TA",
                safe(posting.description()),
                safe(posting.requiredSkills()),
                posting.hoursPerWeek()
        );
    }

    private String buildApplicantBatchPrompt(JobPosting posting, List<MoApplicantView> applicants) {
        String applicantText = applicants.stream()
                .map(applicant -> """
                        Application ID: %s
                        Name: %s
                        Student ID: %s
                        Degree: %s
                        GPA: %s
                        Skills: %s
                        Availability: %s
                        """.formatted(
                        safe(applicant.applicationId()),
                        safe(applicant.fullName()),
                        safe(applicant.studentId()),
                        safe(applicant.degreeProgramme()),
                        safe(applicant.gpa()),
                        safe(applicant.skills()),
                        safe(applicant.availability())
                ))
                .collect(Collectors.joining(System.lineSeparator() + "---" + System.lineSeparator()));

        return """
                Job posting
                Title: %s
                Module: %s
                Type: %s
                Description: %s
                Required skills: %s
                Hours per week: %d

                Applicants
                %s
                """.formatted(
                safe(posting.title()),
                safe(posting.moduleCode()),
                posting.isInvigilation() ? "INVIGILATION" : "TA",
                safe(posting.description()),
                safe(posting.requiredSkills()),
                posting.hoursPerWeek(),
                applicantText
        );
    }

    private String buildWorkloadPrompt(
            TaWorkloadSummary overloaded,
            int threshold,
            List<JobPosting> acceptedJobs,
            Map<String, List<WorkloadCandidateOption>> candidatesByJobId
    ) {
        String jobsText = acceptedJobs.stream()
                .map(job -> """
                        Job ID: %s
                        Title: %s
                        Module: %s
                        Hours: %d
                        Required skills: %s
                        Pending candidates:
                        %s
                        """.formatted(
                        safe(job.jobId()),
                        safe(job.title()),
                        safe(job.moduleCode()),
                        job.hoursPerWeek(),
                        safe(job.requiredSkills()),
                        candidateOptionsText(candidatesByJobId.getOrDefault(job.jobId(), List.of()))
                ))
                .collect(Collectors.joining(System.lineSeparator() + "---" + System.lineSeparator()));

        return """
                Overloaded TA
                Email: %s
                Name: %s
                Student ID: %s
                Assigned modules: %s
                Current weekly hours: %d
                Threshold: %d

                Accepted jobs and possible manual review options
                %s
                """.formatted(
                safe(overloaded.taEmail()),
                safe(overloaded.fullName()),
                safe(overloaded.studentId()),
                safe(overloaded.assignedModules()),
                overloaded.totalWeeklyHours(),
                threshold,
                jobsText
        );
    }

    private String candidateOptionsText(List<WorkloadCandidateOption> options) {
        if (options.isEmpty()) {
            return "No suitable pending candidate under the workload threshold was found.";
        }
        return options.stream()
                .map(option -> """
                        Candidate email: %s
                        Candidate name: %s
                        Current hours: %d
                        Projected hours after assignment: %d
                        AI match score: %d
                        Matched skills: %s
                        Missing skills: %s
                        Recommendation: %s
                        """.formatted(
                        safe(option.candidateEmail()),
                        safe(option.candidateName()),
                        option.currentHours(),
                        option.projectedHours(),
                        option.matchResult().matchScore(),
                        option.matchResult().matchedSkills(),
                        option.matchResult().missingSkills(),
                        safe(option.matchResult().recommendation())
                ))
                .collect(Collectors.joining(System.lineSeparator() + "..." + System.lineSeparator()));
    }

    private SkillMatchResult toSkillMatchResult(JsonNode node) {
        int matchScore = clamp(node.path("matchScore").asInt(0));
        List<String> matchedSkills = toStringList(node.path("matchedSkills"));
        List<String> missingSkills = toStringList(node.path("missingSkills"));
        String recommendation = text(node, "recommendation", "AI recommendation unavailable.");
        return new SkillMatchResult(matchScore, matchedSkills, missingSkills, recommendation);
    }

    private List<String> toStringList(JsonNode node) {
        List<String> values = new ArrayList<>();
        if (node.isArray()) {
            for (JsonNode item : node) {
                String value = item.asText("").trim();
                if (!value.isBlank()) {
                    values.add(value);
                }
            }
        }
        return values;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private String text(JsonNode node, String key, String fallback) {
        String value = node.path(key).asText("").trim();
        return value.isBlank() ? fallback : value;
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "-" : value.trim();
    }
}
