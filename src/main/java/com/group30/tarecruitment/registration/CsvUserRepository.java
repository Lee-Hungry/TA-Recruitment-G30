package com.group30.tarecruitment.registration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class CsvUserRepository {

    private static final String HEADER = "user_id,email,password_hash,role,status,created_at,updated_at";
    private final Path csvPath;

    public CsvUserRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
    }

    public boolean existsByEmail(String email) {
        List<String> lines = readLines();
        return lines.stream().skip(1).anyMatch(line -> {
            String[] parts = line.split(",", -1);
            return parts.length > 1 && parts[1].equalsIgnoreCase(email);
        });
    }

    public void saveTaAccount(TaRegistrationRequest request) {
        String now = OffsetDateTime.now().toString();
        String line = String.join(",",
                "ta-" + UUID.randomUUID(),
                request.email(),
                request.password(),
                "TA",
                "ACTIVE",
                now,
                now
        );
        try {
            Files.writeString(csvPath, line + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save TA account", e);
        }
    }

    private List<String> readLines() {
        ensureFileExists();
        try {
            return Files.readAllLines(csvPath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read users", e);
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
            throw new IllegalStateException("Failed to init users csv", e);
        }
    }
}
