package com.group30.tarecruitment.login;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;

public class CsvUserCredentialRepository {

    private static final String HEADER = "user_id,email,password_hash,role,status,created_at,updated_at";
    private final Path csvPath;

    public CsvUserCredentialRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
    }

    public Optional<UserCredential> findByEmail(String email) {
        ensureFileExists();
        try {
            List<String> lines = Files.readAllLines(csvPath);
            return lines.stream()
                    .skip(1)
                    .map(line -> line.split(",", -1))
                    .filter(parts -> parts.length >= 5)
                    .filter(parts -> parts[1].equalsIgnoreCase(email))
                    .map(parts -> new UserCredential(parts[0], parts[1], parts[2], parts[3], parts[4]))
                    .findFirst();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read user csv", e);
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
            throw new IllegalStateException("Failed to init user csv", e);
        }
    }
}
