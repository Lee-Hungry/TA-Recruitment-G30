package com.group30.tarecruitment.login;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaLoginLogoutServiceTest {

    @Test
    void shouldRevokeSessionAndWriteBackToCsvOnLogout() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-login-logout");
        Path users = tempDir.resolve("user_account.csv");
        Path sessions = tempDir.resolve("session_token.csv");

        Files.writeString(users,
                "user_id,email,password_hash,role,status,created_at,updated_at\n"
                        + "ta-1,ta@g30.local,password123,TA,ACTIVE,2026-03-20T11:00:00+08:00,2026-03-20T11:00:00+08:00\n");

        TaLoginService service = new TaLoginService(
                new CsvUserCredentialRepository(users),
                new CsvSessionTokenRepository(sessions)
        );

        TaLoginResult login = service.login("ta@g30.local", "password123", "127.0.0.1");
        assertTrue(login.success());
        assertTrue(service.logout(login.sessionId()));

        List<String> lines = Files.readAllLines(sessions);
        String sessionLine = lines.stream()
                .skip(1)
                .filter(line -> line.startsWith(login.sessionId() + ","))
                .findFirst()
                .orElseThrow();
        String[] parts = sessionLine.split(",", -1);
        assertFalse(parts[3].isBlank());
        assertFalse(parts[4].isBlank());
    }

    @Test
    void shouldReturnInvalidCredentialWhenPasswordMismatch() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-login-invalid");
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

    @Test
    void shouldReturnDisabledWhenAccountInactive() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-login-disabled");
        Path users = tempDir.resolve("user_account.csv");
        Path sessions = tempDir.resolve("session_token.csv");

        Files.writeString(users,
                "user_id,email,password_hash,role,status,created_at,updated_at\n"
                        + "ta-1,ta@g30.local,password123,TA,INACTIVE,2026-03-20T11:00:00+08:00,2026-03-20T11:00:00+08:00\n");

        TaLoginService service = new TaLoginService(
                new CsvUserCredentialRepository(users),
                new CsvSessionTokenRepository(sessions)
        );

        TaLoginResult result = service.login("ta@g30.local", "password123", "127.0.0.1");
        assertEquals(LoginErrorCode.ACCOUNT_DISABLED, result.errorCode());
    }
}
