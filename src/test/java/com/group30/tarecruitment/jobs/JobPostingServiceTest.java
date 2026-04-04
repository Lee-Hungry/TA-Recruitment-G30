package com.group30.tarecruitment.jobs;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JobPostingServiceTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-29T08:00:00Z"), ZoneId.of("Asia/Shanghai"));

    @Test
    void shouldCreatePostingAndReturnOnlyMoOwnedRowsInMyPostings() throws Exception {
        Path tempDir = Files.createTempDirectory("job-posting-create");
        Path jobCsv = tempDir.resolve("job_posting.csv");
        JobPostingService service = new JobPostingService(new CsvJobPostingRepository(jobCsv), fixedClock);

        service.postJob(new JobPostingDraft(
                "mo1@g30.local",
                "Software Engineering TA",
                "EBU6304",
                "Support labs and assignment marking",
                "Java,JUnit,Scrum",
                10,
                LocalDate.of(2026, 4, 8)
        ));
        service.postJob(new JobPostingDraft(
                "mo2@g30.local",
                "Database TA",
                "EBU6201",
                "Support SQL workshops",
                "SQL,PostgreSQL",
                6,
                LocalDate.of(2026, 4, 10)
        ));

        List<JobPosting> myPostings = service.viewPostingsByMo("mo1@g30.local");

        assertEquals(1, myPostings.size());
        assertEquals("EBU6304", myPostings.getFirst().moduleCode());
        assertEquals("OPEN", myPostings.getFirst().status());
    }

    @Test
    void shouldHideExpiredJobsFromTaBrowseList() throws Exception {
        Path tempDir = Files.createTempDirectory("job-posting-browse");
        Path jobCsv = tempDir.resolve("job_posting.csv");
        JobPostingService service = new JobPostingService(new CsvJobPostingRepository(jobCsv), fixedClock);

        service.postJob(new JobPostingDraft(
                "mo1@g30.local",
                "Open TA",
                "EBU6304",
                "Open job",
                "Java",
                8,
                LocalDate.of(2026, 4, 2)
        ));
        service.postJob(new JobPostingDraft(
                "mo1@g30.local",
                "Expired TA",
                "EBU6305",
                "Expired job",
                "Python",
                8,
                LocalDate.of(2026, 3, 20)
        ));

        List<JobPosting> browseJobs = service.browseOpenJobs();

        assertEquals(1, browseJobs.size());
        assertEquals("Open TA", browseJobs.getFirst().title());
    }
}
