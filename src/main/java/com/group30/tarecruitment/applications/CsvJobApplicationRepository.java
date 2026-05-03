package com.group30.tarecruitment.applications;

import com.group30.tarecruitment.csv.CsvSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class CsvJobApplicationRepository {

    private static final String HEADER = "application_id,job_id,ta_email,status,applied_at,updated_at";
    private final Path csvPath;

    public CsvJobApplicationRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
    }

    public void append(JobApplication application) {
        ensureFileExists();
        try {
            Files.writeString(csvPath, toCsv(application) + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append job application", e);
        }
    }

    public List<JobApplication> readAll() {
        ensureFileExists();
        try {
            List<String> lines = Files.readAllLines(csvPath);
            List<JobApplication> applications = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                List<String> parts = CsvSupport.parseRow(line);
                if (parts.size() < 6) {
                    continue;
                }
                applications.add(new JobApplication(
                        parts.get(0),
                        parts.get(1),
                        parts.get(2),
                        parts.get(3),
                        parts.get(4),
                        parts.get(5)
                ));
            }
            return applications;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read job application csv", e);
        }
    }

    public void replace(JobApplication updated) {
        List<JobApplication> applications = readAll();
        List<String> rewritten = new ArrayList<>();
        rewritten.add(HEADER);

        boolean replaced = false;
        for (JobApplication current : applications) {
            if (current.applicationId().equals(updated.applicationId())) {
                rewritten.add(toCsv(updated));
                replaced = true;
            } else {
                rewritten.add(toCsv(current));
            }
        }

        if (!replaced) {
            rewritten.add(toCsv(updated));
        }

        rewrite(rewritten);
    }

    public void deleteByJobId(String jobId) {
        List<JobApplication> applications = readAll();
        List<String> rewritten = new ArrayList<>();
        rewritten.add(HEADER);
        for (JobApplication application : applications) {
            if (!application.jobId().equals(jobId)) {
                rewritten.add(toCsv(application));
            }
        }
        rewrite(rewritten);
    }

    private String toCsv(JobApplication application) {
        return CsvSupport.joinRow(
                application.applicationId(),
                application.jobId(),
                application.taEmail(),
                application.status(),
                application.appliedAt(),
                application.updatedAt()
        );
    }

    private void rewrite(List<String> rewritten) {
        try {
            Files.writeString(
                    csvPath,
                    String.join(System.lineSeparator(), rewritten) + System.lineSeparator(),
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE
            );
        } catch (IOException e) {
            throw new IllegalStateException("Failed to rewrite job application csv", e);
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
            throw new IllegalStateException("Failed to init job application csv", e);
        }
    }
}
