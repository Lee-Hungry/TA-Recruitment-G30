package com.group30.tarecruitment.mo;

public record MoLoginResult(
        boolean success,
        String message,
        String sessionId,
        String route,
        String sessionRole
) {
    public static MoLoginResult fail(String message) {
        return new MoLoginResult(false, message, null, null, null);
    }

    public static MoLoginResult ok(String sessionId) {
        return new MoLoginResult(true, "MO_LOGIN_OK", sessionId, "MO_DASHBOARD", "MO");
    }
}
