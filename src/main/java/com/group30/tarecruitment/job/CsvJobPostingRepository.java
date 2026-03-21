package com.group30.tarecruitment.job;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CsvJobPostingRepository {

    private static final String HEADER = "job_id,created_by,title,module_code,description,required_skills,hours_per_week,deadline_at,job_type,status";
    private final Path csvPath;

    public CsvJobPostingRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
    }

    public void save(JobPosting jobPosting) {
        ensureFileExists();
        String line = String.join(",",
                jobPosting.jobId(),
                jobPosting.createdBy(),
                jobPosting.title(),
                jobPosting.moduleCode(),
                jobPosting.description(),
                jobPosting.requiredSkills(),
                String.valueOf(jobPosting.hoursPerWeek()),
                jobPosting.deadlineAt(),
                jobPosting.jobType(),
                jobPosting.status());
        try {
            String original = Files.readString(csvPath);
            String merged = original + line + System.lineSeparator();
            Path tempFile = csvPath.resolveSibling(csvPath.getFileName() + ".tmp");
            Files.writeString(
                    tempFile,
                    merged,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
            try {
                Files.move(tempFile, csvPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ex) {
                Files.move(tempFile, csvPath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save job posting", e);
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
            throw new IllegalStateException("Failed to init job csv", e);
        }
    }
}
