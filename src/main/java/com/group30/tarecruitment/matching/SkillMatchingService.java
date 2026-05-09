package com.group30.tarecruitment.matching;

import com.group30.tarecruitment.ai.AiSettingsRepository;
import com.group30.tarecruitment.ai.OpenAiJsonClient;
import com.group30.tarecruitment.ai.OpenAiRecruitmentGateway;
import com.group30.tarecruitment.ai.RecruitmentAiGateway;
import com.group30.tarecruitment.applications.MoApplicantView;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.profile.TaProfile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.nio.file.Path;

public class SkillMatchingService {

    private final RecruitmentAiGateway aiGateway;
    private final Map<String, SkillMatchResult> singleAnalysisCache = new HashMap<>();

    public SkillMatchingService() {
        this(new OpenAiRecruitmentGateway(
                new OpenAiJsonClient(new AiSettingsRepository(Path.of("data", "ai.properties")).load())
        ));
    }

    public SkillMatchingService(RecruitmentAiGateway aiGateway) {
        this.aiGateway = aiGateway;
    }

    public SkillMatchResult analyze(TaProfile profile, JobPosting posting) {
        String cacheKey = posting.jobId() + "|" + normalizeText(profile.email()) + "|" + normalizeText(profile.skills());
        SkillMatchResult cached = singleAnalysisCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        SkillMatchResult result;
        try {
            result = aiGateway.analyzeSkillMatch(profile, posting);
        } catch (Exception ex) {
            result = analyze(profile.skills(), posting.requiredSkills());
        }
        singleAnalysisCache.put(cacheKey, result);
        return result;
    }

    public Map<String, SkillMatchResult> analyzeApplicants(JobPosting posting, List<MoApplicantView> applicants) {
        try {
            Map<String, SkillMatchResult> results = aiGateway.analyzeApplicants(posting, applicants);
            if (!results.isEmpty()) {
                return results;
            }
        } catch (Exception ignored) {
        }

        Map<String, SkillMatchResult> fallback = new LinkedHashMap<>();
        for (MoApplicantView applicant : applicants) {
            fallback.put(applicant.applicationId(), analyze(applicant.skills(), posting.requiredSkills()));
        }
        return fallback;
    }

    public SkillMatchResult analyze(String profileSkills, String requiredSkills) {
        Map<String, String> profileTokens = tokenize(profileSkills);
        Map<String, String> requiredTokens = tokenize(requiredSkills);

        if (requiredTokens.isEmpty()) {
            return new SkillMatchResult(
                    100,
                    List.of(),
                    List.of(),
                    "No required skills were listed for this job, so this recommendation is informational only."
            );
        }

        List<String> matched = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (Map.Entry<String, String> required : requiredTokens.entrySet()) {
            if (hasMatchingSkill(required.getKey(), profileTokens.keySet())) {
                matched.add(required.getValue());
            } else {
                missing.add(required.getValue());
            }
        }

        int matchScore = (int) Math.round(matched.size() * 100.0 / requiredTokens.size());
        return new SkillMatchResult(matchScore, matched, missing, buildRecommendation(matchScore, matched, missing));
    }

    public List<MoApplicantView> sortApplicantsByMatch(JobPosting posting, List<MoApplicantView> applicants) {
        Map<String, SkillMatchResult> matches = analyzeApplicants(posting, applicants);
        return applicants.stream()
                .sorted(Comparator.comparingInt((MoApplicantView applicant) ->
                        matches.getOrDefault(applicant.applicationId(), analyze(applicant.skills(), posting.requiredSkills())).matchScore()
                ).reversed())
                .toList();
    }

    private String buildRecommendation(int matchScore, List<String> matched, List<String> missing) {
        if (missing.isEmpty()) {
            return "Strong recommendation: your current profile covers all listed required skills.";
        }
        if (matched.isEmpty()) {
            return "Low match: update your profile before applying, because none of the listed required skills were detected.";
        }
        if (matchScore >= 70) {
            return "Promising match: you cover most of the listed skills, but the missing items should be addressed in your profile or CV.";
        }
        if (matchScore >= 40) {
            return "Partial match: you meet some requirements, but the missing skills may weaken the application.";
        }
        return "High skill gap: the current profile only overlaps slightly with the listed requirements.";
    }

    private boolean hasMatchingSkill(String requiredToken, Set<String> profileTokens) {
        for (String profileToken : profileTokens) {
            if (profileToken.equals(requiredToken)) {
                return true;
            }
            if (profileToken.contains(requiredToken) || requiredToken.contains(profileToken)) {
                return true;
            }
            if (sharesMeaningfulWord(requiredToken, profileToken)) {
                return true;
            }
        }
        return false;
    }

    private boolean sharesMeaningfulWord(String left, String right) {
        Set<String> leftWords = splitWords(left);
        Set<String> rightWords = splitWords(right);
        for (String word : leftWords) {
            if (word.length() >= 4 && rightWords.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> splitWords(String value) {
        Set<String> words = new LinkedHashSet<>();
        for (String token : value.split("\\s+")) {
            String normalized = normalizeToken(token);
            if (!normalized.isBlank()) {
                words.add(normalized);
            }
        }
        return words;
    }

    private Map<String, String> tokenize(String rawSkills) {
        Map<String, String> tokens = new LinkedHashMap<>();
        if (rawSkills == null || rawSkills.isBlank()) {
            return tokens;
        }
        for (String token : rawSkills.split("[,;\\n\\r/|]+")) {
            String display = token.trim();
            String normalized = normalizeToken(display);
            if (!display.isBlank() && !normalized.isBlank()) {
                tokens.putIfAbsent(normalized, display);
            }
        }
        return tokens;
    }

    private String normalizeToken(String value) {
        return value == null
                ? ""
                : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9#+]+", "");
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
