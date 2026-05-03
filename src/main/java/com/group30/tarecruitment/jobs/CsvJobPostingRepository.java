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

    private static final String HEADER = "job_id,posted_by_email,title,module_code,description,required_skills,hours_per_week,application_deadline,status,created_at,updated_at,job_type,exam_date,exam_time,location,invigilators_needed";
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
                LocalDate examDate = parts.size() > 12 && !parts.get(12).isBlank() ? LocalDate.parse(parts.get(12)) : null;
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
                        parts.get(10),
                        parts.size() > 11 && !parts.get(11).isBlank() ? parts.get(11) : "TA",
                        examDate,
                        parts.size() > 13 ? parts.get(13) : "",
                        parts.size() > 14 ? parts.get(14) : "",
                        parts.size() > 15 && !parts.get(15).isBlank() ? Integer.parseInt(parts.get(15)) : 0
                ));
            }
            return postings;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read job posting csv", e);
        }
    }

    public void replace(JobPosting updated) {
        List<JobPosting> postings = readAll();
        List<String> rewritten = new ArrayList<>();
        rewritten.add(HEADER);

        boolean replaced = false;
        for (JobPosting current : postings) {
            if (current.jobId().equals(updated.jobId())) {
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

    public void deleteById(String jobId) {
        List<JobPosting> postings = readAll();
        List<String> rewritten = new ArrayList<>();
        rewritten.add(HEADER);
        for (JobPosting posting : postings) {
            if (!posting.jobId().equals(jobId)) {
                rewritten.add(toCsv(posting));
            }
        }
        rewrite(rewritten);
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
                posting.updatedAt(),
                posting.jobType(),
                posting.examDate() == null ? "" : posting.examDate().toString(),
                posting.examTime(),
                posting.location(),
                Integer.toString(posting.invigilatorsNeeded())
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
            throw new IllegalStateException("Failed to rewrite job posting csv", e);
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
            throw new IllegalStateException("Failed to init job posting csv", e);
        }
    }
}
