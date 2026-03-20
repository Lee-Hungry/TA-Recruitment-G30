package com.group30.tarecruitment.auth.repository;

import com.group30.tarecruitment.auth.SessionToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CsvSessionTokenRepository {

    private static final String HEADER = "session_id,user_id,issued_at,expired_at,revoked_at,client_ip";
    private final Path csvPath;

    public CsvSessionTokenRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
    }

    public void append(SessionToken token) {
        ensureFileExists();
        String line = String.join(",",
                token.sessionId(),
                token.userId(),
                token.issuedAt(),
                token.expiredAt(),
                token.revokedAt(),
                token.clientIp());
        try {
            Files.writeString(csvPath, line + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append session", e);
        }
    }

    private void ensureFileExists() {
        try {
            if (csvPath.getParent() != null) {
                Files.createDirectories(csvPath.getParent());
            }
            if (!Files.exists(csvPath)) {
                Files.writeString(csvPath, HEADER + System.lineSeparator(), StandardOpenOption.CREATE_NEW);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to init session csv", e);
        }
    }
}
