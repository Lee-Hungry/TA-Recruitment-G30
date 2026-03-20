package com.group30.tarecruitment.job;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class JobPostingServiceTest {

    @Test
    void shouldPersistModuleHoursAndDeadlineFields() throws Exception {
        Path tempDir = Files.createTempDirectory("job-posting");
        Path csv = tempDir.resolve("job_posting.csv");

        JobPostingService service = new JobPostingService(new CsvJobPostingRepository(csv));
        service.publish(new JobPostingRequest(
                "mo-001",
                "TA for Software Engineering",
                "EBU6304",
                "Support labs",
                "Java;CSV",
                10,
                "2026-04-10T23:59:59+08:00",
                "TA"
        ));

        String all = Files.readString(csv);
        assertTrue(all.contains("EBU6304"));
        assertTrue(all.contains(",10,"));
        assertTrue(all.contains("2026-04-10T23:59:59+08:00"));
    }
}
