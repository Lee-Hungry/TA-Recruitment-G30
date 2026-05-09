package com.group30.tarecruitment.matching;

import java.util.List;

public record SkillMatchResult(
        int matchScore,
        List<String> matchedSkills,
        List<String> missingSkills,
        String recommendation
) {

    public boolean hasMissingSkills() {
        return !missingSkills.isEmpty();
    }

    public boolean isStrongMatch() {
        return matchScore >= 80;
    }
}
