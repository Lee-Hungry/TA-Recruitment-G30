package com.group30.tarecruitment.admin;

import com.group30.tarecruitment.matching.SkillMatchResult;

public record WorkloadCandidateOption(
        String candidateEmail,
        String candidateName,
        int currentHours,
        int projectedHours,
        SkillMatchResult matchResult
) {
}
