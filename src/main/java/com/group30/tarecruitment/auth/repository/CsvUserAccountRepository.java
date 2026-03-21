package com.group30.tarecruitment.auth.repository;

import com.group30.tarecruitment.auth.AuthRole;
import com.group30.tarecruitment.auth.UserAccount;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CsvUserAccountRepository {

    private static final String HEADER = "user_id,email,password_hash,role,status,created_at,updated_at";
    private final Path csvPath;

    public CsvUserAccountRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
    }

    public Optional<UserAccount> findByEmail(String email) {
        return readAll().stream().filter(user -> user.email().equalsIgnoreCase(email)).findFirst();
    }

    public List<UserAccount> readAll() {
        ensureFileExists();
        try {
            List<String> lines = Files.readAllLines(csvPath);
            List<UserAccount> users = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 7) {
                    continue;
                }
                users.add(new UserAccount(
                        parts[0],
                        parts[1],
                        parts[2],
                        AuthRole.fromText(parts[3]),
                        parts[4],
                        parts[5],
                        parts[6]
                ));
            }
            return users;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read user csv", e);
        }
    }

    public void append(UserAccount userAccount) {
        ensureFileExists();
        String line = String.join(",",
                userAccount.userId(),
                userAccount.email(),
                userAccount.passwordHash(),
                userAccount.role().name(),
                userAccount.status(),
                userAccount.createdAt(),
                userAccount.updatedAt());
        try {
            Files.writeString(csvPath, line + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append user", e);
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
