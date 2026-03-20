package com.group30.tarecruitment.profile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CsvTaProfileRepository {

    private static final String HEADER = "ta_profile_id,user_id,full_name,student_id,degree_programme,gpa,skills,availability,cv_file_path";
    private final Path csvPath;

    public CsvTaProfileRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
    }

    public Optional<TaProfile> findByUserId(String userId) {
        return readAll().stream().filter(profile -> profile.userId().equals(userId)).findFirst();
    }

    public void save(TaProfile profile) {
        ensureFileExists();
        String line = String.join(",",
                profile.taProfileId(),
                profile.userId(),
                profile.fullName(),
                profile.studentId(),
                profile.degreeProgramme(),
                profile.gpa(),
                profile.skills(),
                profile.availability(),
                profile.cvFilePath());
        try {
            Files.writeString(csvPath, line + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save TA profile", e);
        }
    }

    public List<TaProfile> readAll() {
        ensureFileExists();
        try {
            List<TaProfile> profiles = new ArrayList<>();
            List<String> lines = Files.readAllLines(csvPath);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(",", -1);
                if (parts.length < 9) {
                    continue;
                }
                profiles.add(new TaProfile(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], parts[7], parts[8]));
            }
            return profiles;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read profile csv", e);
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
            throw new IllegalStateException("Failed to init profile csv", e);
        }
    }
}
