package com.group30.tarecruitment.admin;

public record TaWorkloadSummary(
        String taEmail,
        String fullName,
        String studentId,
        String assignedModules,
        int totalWeeklyHours,
        boolean overloaded
) {
}
