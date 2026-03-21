package com.group30.tarecruitment.job;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
                OffsetDateTime.now().plusDays(7).toString(),
                "TA"
        ));

        String all = Files.readString(csv);
        assertTrue(all.contains("EBU6304"));
        assertTrue(all.contains(",10,"));
        assertTrue(all.contains("TA for Software Engineering"));
    }

    @Test
    void shouldRejectInvalidHoursRange() throws Exception {
        Path tempDir = Files.createTempDirectory("job-posting-hours");
        Path csv = tempDir.resolve("job_posting.csv");
        JobPostingService service = new JobPostingService(new CsvJobPostingRepository(csv));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.publish(new JobPostingRequest(
                        "mo-001",
                        "TA for Software Engineering",
                        "EBU6304",
                        "Support labs",
                        "Java;CSV",
                        40,
                        OffsetDateTime.now().plusDays(7).toString(),
                        "TA"
                )));

        assertEquals("INVALID_HOURS_PER_WEEK", ex.getMessage());
    }

    @Test
    void shouldRejectPastDeadline() throws Exception {
        Path tempDir = Files.createTempDirectory("job-posting-deadline");
        Path csv = tempDir.resolve("job_posting.csv");
        JobPostingService service = new JobPostingService(new CsvJobPostingRepository(csv));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.publish(new JobPostingRequest(
                        "mo-001",
                        "TA for Software Engineering",
                        "EBU6304",
                        "Support labs",
                        "Java;CSV",
                        8,
                        OffsetDateTime.now().minusDays(1).toString(),
                        "TA"
                )));

        assertEquals("DEADLINE_MUST_BE_FUTURE", ex.getMessage());
    }
}
