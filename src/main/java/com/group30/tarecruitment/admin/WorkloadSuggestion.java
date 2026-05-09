package com.group30.tarecruitment.admin;

public record WorkloadSuggestion(
        String title,
        String detail,
        boolean actionable,
        String overloadedTaEmail,
        String suggestedCandidateEmail,
        String suggestedJobId
) {

    @Override
    public String toString() {
        return title;
    }
}
