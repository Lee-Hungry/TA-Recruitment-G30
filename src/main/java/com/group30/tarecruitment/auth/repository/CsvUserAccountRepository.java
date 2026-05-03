package com.group30.tarecruitment.auth.repository;

import com.group30.tarecruitment.auth.AuthRole;
import com.group30.tarecruitment.auth.UserAccount;
import com.group30.tarecruitment.csv.CsvSupport;

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
                List<String> parts = CsvSupport.parseRow(line);
                if (parts.size() < 7) {
                    continue;
                }
                users.add(new UserAccount(
                        parts.get(0),
                        parts.get(1),
                        parts.get(2),
                        AuthRole.fromText(parts.get(3)),
                        parts.get(4),
                        parts.get(5),
                        parts.get(6)
                ));
            }
            return users;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read user csv", e);
        }
    }

    public void append(UserAccount userAccount) {
        ensureFileExists();
        try {
            Files.writeString(csvPath, toCsv(userAccount) + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append user", e);
        }
    }

    public void replace(UserAccount updated) {
        List<UserAccount> users = readAll();
        List<String> rewritten = new ArrayList<>();
        rewritten.add(HEADER);

        boolean replaced = false;
        for (UserAccount current : users) {
            if (current.userId().equals(updated.userId())) {
                rewritten.add(toCsv(updated));
                replaced = true;
            } else {
                rewritten.add(toCsv(current));
            }
        }

        if (!replaced) {
            rewritten.add(toCsv(updated));
        }

        try {
            Files.writeString(
                    csvPath,
                    String.join(System.lineSeparator(), rewritten) + System.lineSeparator(),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to rewrite user csv", e);
        }
    }

    private String toCsv(UserAccount userAccount) {
        return CsvSupport.joinRow(
                userAccount.userId(),
                userAccount.email(),
                userAccount.passwordHash(),
                userAccount.role().name(),
                userAccount.status(),
                userAccount.createdAt(),
                userAccount.updatedAt()
        );
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
