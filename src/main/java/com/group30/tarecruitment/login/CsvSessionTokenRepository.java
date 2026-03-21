package com.group30.tarecruitment.login;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class CsvSessionTokenRepository {

    private static final String HEADER = "session_id,user_id,issued_at,expired_at,revoked_at,client_ip";
    private final Path csvPath;

    public CsvSessionTokenRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
    }

    public void append(String sessionId, String userId, String issuedAt, String clientIp) {
        String line = String.join(",", sessionId, userId, issuedAt, "", "", clientIp);
        try {
            Files.writeString(csvPath, line + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append session", e);
        }
    }

    public boolean revoke(String sessionId, String revokedAt) {
        ensureFileExists();
        try {
            List<String> lines = Files.readAllLines(csvPath);
            if (lines.isEmpty()) {
                return false;
            }

            boolean updated = false;
            List<String> rewritten = new ArrayList<>();
            rewritten.add(lines.get(0));

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 6) {
                    continue;
                }
                if (parts[0].equals(sessionId)) {
                    parts[3] = revokedAt;
                    parts[4] = revokedAt;
                    updated = true;
                }
                rewritten.add(String.join(",", parts));
            }

            if (!updated) {
                return false;
            }

            Files.writeString(
                    csvPath,
                    String.join(System.lineSeparator(), rewritten) + System.lineSeparator(),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            return true;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to revoke session", e);
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
