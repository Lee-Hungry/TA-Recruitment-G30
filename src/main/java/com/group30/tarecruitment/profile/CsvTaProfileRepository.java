package com.group30.tarecruitment.profile;

import com.group30.tarecruitment.csv.CsvSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CsvTaProfileRepository {

    private static final String HEADER = "email,full_name,student_id,contact_email,degree_programme,gpa,skills,availability,updated_at";
    private final Path csvPath;

    public CsvTaProfileRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
    }

    public Optional<TaProfile> findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        return readAll().stream()
                .filter(profile -> profile.email().equalsIgnoreCase(normalizedEmail))
                .findFirst();
    }

    public void upsert(TaProfile profile) {
        List<TaProfile> profiles = readAll();
        List<String> rewritten = new ArrayList<>();
        rewritten.add(HEADER);

        boolean replaced = false;
        for (TaProfile existing : profiles) {
            if (existing.email().equalsIgnoreCase(profile.email())) {
                rewritten.add(toCsv(profile));
                replaced = true;
            } else {
                rewritten.add(toCsv(existing));
            }
        }

        if (!replaced) {
            rewritten.add(toCsv(profile));
        }

        try {
            Files.writeString(
                    csvPath,
                    String.join(System.lineSeparator(), rewritten) + System.lineSeparator(),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write TA profile csv", e);
        }
    }

    private List<TaProfile> readAll() {
        ensureFileExists();
        try {
            List<String> lines = Files.readAllLines(csvPath);
            List<TaProfile> profiles = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                List<String> parts = CsvSupport.parseRow(line);
                if (parts.size() < 9) {
                    continue;
                }
                profiles.add(new TaProfile(
                        parts.get(0),
                        parts.get(1),
                        parts.get(2),
                        parts.get(3),
                        parts.get(4),
                        parts.get(5),
                        parts.get(6),
                        parts.get(7),
                        parts.get(8)
                ));
            }
            return profiles;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read TA profile csv", e);
        }
    }

    private String toCsv(TaProfile profile) {
        return CsvSupport.joinRow(
                profile.email(),
                profile.fullName(),
                profile.studentId(),
                profile.contactEmail(),
                profile.degreeProgramme(),
                profile.gpa(),
                profile.skills(),
                profile.availability(),
                profile.updatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
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
            throw new IllegalStateException("Failed to init TA profile csv", e);
        }
    }
}
