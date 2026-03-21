package com.group30.tarecruitment.auth;

public record AuthResult(
        boolean success,
        String error,
        String sessionId,
        String route
) {
    public static AuthResult fail(String error) {
        return new AuthResult(false, error, null, null);
    }

    public static AuthResult ok(String sessionId, String route) {
        return new AuthResult(true, null, sessionId, route);
    }
}
