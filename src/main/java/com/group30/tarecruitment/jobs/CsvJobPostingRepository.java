package com.group30.tarecruitment.jobs;

import com.group30.tarecruitment.csv.CsvSupport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CsvJobPostingRepository {

    private static final String HEADER = "job_id,posted_by_email,title,module_code,description,required_skills,hours_per_week,application_deadline,status,created_at,updated_at";
    private final Path csvPath;

    public CsvJobPostingRepository(Path csvPath) {
        this.csvPath = csvPath;
        ensureFileExists();
    }

    public void append(JobPosting posting) {
        ensureFileExists();
        try {
            Files.writeString(csvPath, toCsv(posting) + System.lineSeparator(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to append job posting", e);
        }
    }

    public List<JobPosting> readAll() {
        ensureFileExists();
        try {
            List<String> lines = Files.readAllLines(csvPath);
            List<JobPosting> postings = new ArrayList<>();
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                List<String> parts = CsvSupport.parseRow(line);
                if (parts.size() < 11) {
                    continue;
                }
                postings.add(new JobPosting(
                        parts.get(0),
                        parts.get(1),
                        parts.get(2),
                        parts.get(3),
                        parts.get(4),
                        parts.get(5),
                        Integer.parseInt(parts.get(6)),
                        LocalDate.parse(parts.get(7)),
                        parts.get(8),
                        parts.get(9),
                        parts.get(10)
                ));
            }
            return postings;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read job posting csv", e);
        }
    }

    private String toCsv(JobPosting posting) {
        return CsvSupport.joinRow(
                posting.jobId(),
                posting.postedByEmail(),
                posting.title(),
                posting.moduleCode(),
                posting.description(),
                posting.requiredSkills(),
                Integer.toString(posting.hoursPerWeek()),
                posting.applicationDeadline().toString(),
                posting.status(),
                posting.createdAt(),
                posting.updatedAt()
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
            throw new IllegalStateException("Failed to init job posting csv", e);
        }
    }
}
