package com.group30.tarecruitment.mo;

public record MoSession(
        String sessionId,
        String userId,
        String role,
        String issuedAt,
        String expiredAt
) {
    public boolean expired() {
        return expiredAt != null && !expiredAt.isBlank();
    }
}
