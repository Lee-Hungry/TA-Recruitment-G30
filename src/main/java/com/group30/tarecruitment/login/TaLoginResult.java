package com.group30.tarecruitment.login;

public record TaLoginResult(
        boolean success,
        LoginErrorCode errorCode,
        String sessionId
) {
    public static TaLoginResult fail(LoginErrorCode code) {
        return new TaLoginResult(false, code, null);
    }

    public static TaLoginResult ok(String sessionId) {
        return new TaLoginResult(true, null, sessionId);
    }
}
