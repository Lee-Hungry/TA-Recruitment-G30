package com.group30.tarecruitment.auth;

import com.group30.tarecruitment.auth.repository.CsvSessionTokenRepository;
import com.group30.tarecruitment.auth.repository.CsvUserAccountRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {

    @Test
    void shouldLoginAndPersistSession() throws Exception {
        Path tempDir = Files.createTempDirectory("auth-test");
        Path userCsv = tempDir.resolve("user_account.csv");
        Path sessionCsv = tempDir.resolve("session_token.csv");

        CsvUserAccountRepository users = new CsvUserAccountRepository(userCsv);
        users.append(new UserAccount("u-001", "admin@g30.local", "admin123", AuthRole.ADMIN,
                "ACTIVE", "2026-03-20T10:00:00+08:00", "2026-03-20T10:00:00+08:00"));
        AuthService service = new AuthService(users, new CsvSessionTokenRepository(sessionCsv));

        AuthResult result = service.login("admin@g30.local", "admin123", AuthRole.ADMIN, "127.0.0.1");

        assertTrue(result.success());
        assertEquals("ADMIN_DASHBOARD", result.route());
        assertTrue(Files.readAllLines(sessionCsv).size() >= 2);
    }

    @Test
    void shouldFailOnRoleMismatch() throws Exception {
        Path tempDir = Files.createTempDirectory("auth-test-2");
        Path userCsv = tempDir.resolve("user_account.csv");
        Path sessionCsv = tempDir.resolve("session_token.csv");

        CsvUserAccountRepository users = new CsvUserAccountRepository(userCsv);
        users.append(new UserAccount("u-002", "mo@g30.local", "pass12345", AuthRole.MO,
                "ACTIVE", "2026-03-20T10:00:00+08:00", "2026-03-20T10:00:00+08:00"));
        AuthService service = new AuthService(users, new CsvSessionTokenRepository(sessionCsv));

        AuthResult result = service.login("mo@g30.local", "pass12345", AuthRole.ADMIN, "127.0.0.1");

        assertFalse(result.success());
        assertEquals("ROLE_MISMATCH", result.error());
    }
}
