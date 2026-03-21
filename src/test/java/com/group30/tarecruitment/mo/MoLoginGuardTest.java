package com.group30.tarecruitment.mo;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoLoginGuardTest {

    @Test
    void shouldAllowMoSessionToAccessDashboard() throws Exception {
        Path tempDir = Files.createTempDirectory("mo-guard-allow");
        Path users = tempDir.resolve("user_account.csv");
        Path sessions = tempDir.resolve("session_token.csv");

        Files.writeString(users,
                "user_id,email,password_hash,role,status,created_at,updated_at\n"
                        + "mo-1,mo@g30.local,mo123456,MO,ACTIVE,2026-03-20T11:00:00+08:00,2026-03-20T11:00:00+08:00\n");

        MoLoginService service = new MoLoginService(new CsvMoAccountRepository(users), new CsvSessionRepository(sessions));
        MoLoginResult login = service.login("mo@g30.local", "mo123456");

        assertTrue(service.canAccessMoDashboard(login.sessionId(), login.sessionRole()));
    }

    @Test
    void shouldBlockTaOrAdminRoleFromMoDashboard() throws Exception {
        Path tempDir = Files.createTempDirectory("mo-guard-role");
        Path users = tempDir.resolve("user_account.csv");
        Path sessions = tempDir.resolve("session_token.csv");

        Files.writeString(users,
                "user_id,email,password_hash,role,status,created_at,updated_at\n"
                        + "mo-1,mo@g30.local,mo123456,MO,ACTIVE,2026-03-20T11:00:00+08:00,2026-03-20T11:00:00+08:00\n");

        MoLoginService service = new MoLoginService(new CsvMoAccountRepository(users), new CsvSessionRepository(sessions));
        MoLoginResult login = service.login("mo@g30.local", "mo123456");

        assertFalse(service.canAccessMoDashboard(login.sessionId(), "TA"));
        assertFalse(service.canAccessMoDashboard(login.sessionId(), "ADMIN"));
    }

    @Test
    void shouldBlockExpiredSession() throws Exception {
        Path tempDir = Files.createTempDirectory("mo-guard-expired");
        Path users = tempDir.resolve("user_account.csv");
        Path sessions = tempDir.resolve("session_token.csv");

        Files.writeString(users,
                "user_id,email,password_hash,role,status,created_at,updated_at\n"
                        + "mo-1,mo@g30.local,mo123456,MO,ACTIVE,2026-03-20T11:00:00+08:00,2026-03-20T11:00:00+08:00\n");

        MoLoginService service = new MoLoginService(new CsvMoAccountRepository(users), new CsvSessionRepository(sessions));
        MoLoginResult login = service.login("mo@g30.local", "mo123456");
        assertTrue(service.expireSession(login.sessionId()));

        assertFalse(service.canAccessMoDashboard(login.sessionId(), login.sessionRole()));
    }
}
