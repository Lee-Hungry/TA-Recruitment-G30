package com.group30.tarecruitment.applications;

import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.jobs.JobPostingDraft;
import com.group30.tarecruitment.jobs.JobPostingService;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobApplicationServiceTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-29T08:00:00Z"), ZoneId.of("Asia/Shanghai"));

    @Test
    void shouldCreatePendingApplicationAndExposeItInTaStatusView() throws Exception {
        Path tempDir = Files.createTempDirectory("job-application-create");
        Path jobCsv = tempDir.resolve("job_posting.csv");
        Path applicationCsv = tempDir.resolve("job_application.csv");

        JobPostingService jobPostingService = new JobPostingService(new CsvJobPostingRepository(jobCsv), fixedClock);
        JobPosting posting = jobPostingService.postJob(new JobPostingDraft(
                "mo@g30.local",
                "Software Engineering TA",
                "EBU6304",
                "Support labs and marking",
                "Java,JUnit,Scrum",
                10,
                LocalDate.of(2026, 4, 15)
        ));

        JobApplicationService service = new JobApplicationService(
                new CsvJobApplicationRepository(applicationCsv),
                new CsvJobPostingRepository(jobCsv),
                fixedClock
        );

        JobApplication application = service.submitApplication("ta@g30.local", posting.jobId());
        List<TaApplicationSummary> statusView = service.listApplicationsForTa("ta@g30.local");

        assertEquals("PENDING", application.status());
        assertEquals(1, statusView.size());
        assertEquals("Software Engineering TA", statusView.getFirst().jobTitle());
        assertEquals("EBU6304", statusView.getFirst().moduleCode());
        assertEquals("PENDING", statusView.getFirst().status());
    }

    @Test
    void shouldRejectDuplicateApplicationsForSameJob() throws Exception {
        Path tempDir = Files.createTempDirectory("job-application-duplicate");
        Path jobCsv = tempDir.resolve("job_posting.csv");
        Path applicationCsv = tempDir.resolve("job_application.csv");

        JobPostingService jobPostingService = new JobPostingService(new CsvJobPostingRepository(jobCsv), fixedClock);
        JobPosting posting = jobPostingService.postJob(new JobPostingDraft(
                "mo@g30.local",
                "Database TA",
                "EBU6201",
                "Support SQL workshops",
                "SQL,PostgreSQL",
                8,
                LocalDate.of(2026, 4, 15)
        ));

        JobApplicationService service = new JobApplicationService(
                new CsvJobApplicationRepository(applicationCsv),
                new CsvJobPostingRepository(jobCsv),
                fixedClock
        );
        service.submitApplication("ta@g30.local", posting.jobId());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.submitApplication("ta@g30.local", posting.jobId())
        );

        assertEquals("DUPLICATE_APPLICATION", error.getMessage());
    }
}
