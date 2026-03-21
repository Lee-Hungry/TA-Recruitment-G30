package com.group30.tarecruitment.mo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    public Optional<MoSession> findBySessionId(String sessionId) {
        ensureFileExists();
        try {
            List<String> lines = Files.readAllLines(csvPath);
            return lines.stream()
                    .skip(1)
                    .map(line -> line.split(",", -1))
                    .filter(parts -> parts.length >= 5)
                    .filter(parts -> parts[0].equals(sessionId))
                    .map(parts -> new MoSession(parts[0], parts[1], parts[2], parts[3], parts[4]))
                    .findFirst();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read session csv", e);
        }
    }

    public boolean expireSessionNow(String sessionId) {
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
                String raw = lines.get(i).trim();
                if (raw.isEmpty()) {
                    continue;
                }
                String[] parts = raw.split(",", -1);
                if (parts.length < 5) {
                    continue;
                }
                if (parts[0].equals(sessionId)) {
                    parts[4] = OffsetDateTime.now().toString();
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
            throw new IllegalStateException("Failed to expire session", e);
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
