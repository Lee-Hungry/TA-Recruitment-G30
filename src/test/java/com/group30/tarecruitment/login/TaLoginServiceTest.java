package com.group30.tarecruitment.login;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaLoginServiceTest {

    @Test
    void shouldWriteSessionOnSuccessfulLogin() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-login");
        Path users = tempDir.resolve("user_account.csv");
        Path sessions = tempDir.resolve("session_token.csv");

        Files.writeString(users,
                "user_id,email,password_hash,role,status,created_at,updated_at\n"
                        + "ta-1,ta@g30.local,password123,TA,ACTIVE,2026-03-20T11:00:00+08:00,2026-03-20T11:00:00+08:00\n");

        TaLoginService service = new TaLoginService(
                new CsvUserCredentialRepository(users),
                new CsvSessionTokenRepository(sessions)
        );

        TaLoginResult result = service.login("ta@g30.local", "password123", "127.0.0.1");

        assertTrue(result.success());
        assertTrue(Files.readAllLines(sessions).size() >= 2);
    }

    @Test
    void shouldReturnTraceableErrorCode() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-login-fail");
        Path users = tempDir.resolve("user_account.csv");
        Path sessions = tempDir.resolve("session_token.csv");

        Files.writeString(users,
                "user_id,email,password_hash,role,status,created_at,updated_at\n"
                        + "ta-1,ta@g30.local,password123,TA,ACTIVE,2026-03-20T11:00:00+08:00,2026-03-20T11:00:00+08:00\n");

        TaLoginService service = new TaLoginService(
                new CsvUserCredentialRepository(users),
                new CsvSessionTokenRepository(sessions)
        );

        TaLoginResult result = service.login("ta@g30.local", "wrong-pass", "127.0.0.1");

        assertEquals(LoginErrorCode.INVALID_CREDENTIAL, result.errorCode());
    }
}
