package com.group30.tarecruitment.auth;

public record SessionToken(
        String sessionId,
        String userId,
        String issuedAt,
        String expiredAt,
        String revokedAt,
        String clientIp
) {
}
