package com.group30.tarecruitment.mo;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoLoginServiceTest {

    @Test
    void shouldLoadPreseededMoAccountAndLogin() throws Exception {
        Path tempDir = Files.createTempDirectory("mo-login");
        Path users = tempDir.resolve("user_account.csv");
        Path sessions = tempDir.resolve("session_token.csv");

        Files.writeString(users,
                "user_id,email,password_hash,role,status,created_at,updated_at\n"
                        + "mo-1,mo@g30.local,mo123456,MO,ACTIVE,2026-03-20T11:00:00+08:00,2026-03-20T11:00:00+08:00\n");

        MoLoginService service = new MoLoginService(new CsvMoAccountRepository(users), new CsvSessionRepository(sessions));
        MoLoginResult result = service.login("mo@g30.local", "mo123456");

        assertTrue(result.success());
        assertEquals("MO_DASHBOARD", result.route());
        assertTrue(Files.readAllLines(sessions).size() >= 2);
    }

    @Test
    void shouldReturnFailureMessageOnBadCredential() throws Exception {
        Path tempDir = Files.createTempDirectory("mo-login-fail");
        Path users = tempDir.resolve("user_account.csv");
        Path sessions = tempDir.resolve("session_token.csv");

        Files.writeString(users,
                "user_id,email,password_hash,role,status,created_at,updated_at\n"
                        + "mo-1,mo@g30.local,mo123456,MO,ACTIVE,2026-03-20T11:00:00+08:00,2026-03-20T11:00:00+08:00\n");

        MoLoginService service = new MoLoginService(new CsvMoAccountRepository(users), new CsvSessionRepository(sessions));
        MoLoginResult result = service.login("mo@g30.local", "wrong");

        assertEquals("AUTH_FAILED_INVALID_CREDENTIAL", result.message());
    }
}
