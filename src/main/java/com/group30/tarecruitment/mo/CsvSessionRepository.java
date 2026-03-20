package com.group30.tarecruitment.mo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.UUID;

public class CsvSessionRepository {

    private static final String HEADER = "session_id,user_id,role,issued_at,expired_at";
    private final Path csvPath;

    public CsvSessionRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
    }

    public String createMoSession(String userId) {
        String sessionId = UUID.randomUUID().toString();
        String line = String.join(",", sessionId, userId, "MO", OffsetDateTime.now().toString(), "");
        try {
            Files.writeString(csvPath, line + System.lineSeparator(), StandardOpenOption.APPEND);
            return sessionId;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write session", e);
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
