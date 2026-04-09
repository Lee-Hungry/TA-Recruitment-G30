package com.group30.tarecruitment.applications;

import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.jobs.JobPostingDraft;
import com.group30.tarecruitment.jobs.JobPostingService;
import com.group30.tarecruitment.profile.CsvTaProfileRepository;
import com.group30.tarecruitment.profile.TaProfileDraft;
import com.group30.tarecruitment.profile.TaProfileService;
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
        Path profileCsv = tempDir.resolve("ta_profile.csv");
        Path applicationCsv = tempDir.resolve("job_application.csv");

        JobPostingService jobPostingService = new JobPostingService(new CsvJobPostingRepository(jobCsv), fixedClock);
        TaProfileService profileService = new TaProfileService(new CsvTaProfileRepository(profileCsv));
        JobPosting posting = jobPostingService.postJob(new JobPostingDraft(
                "mo@g30.local",
                "Software Engineering TA",
                "EBU6304",
                "Support labs and marking",
                "Java,JUnit,Scrum",
                10,
                LocalDate.of(2026, 4, 15)
        ));
        profileService.saveProfile("ta@g30.local", new TaProfileDraft(
                "Alice Zhang",
                "231222001",
                "alice@g30.local",
                "MSc Software Engineering",
                "3.82",
                "Java,Communication",
                "Weekdays after 2pm",
                ""
        ));

        JobApplicationService service = new JobApplicationService(
                new CsvJobApplicationRepository(applicationCsv),
                new CsvJobPostingRepository(jobCsv),
                new CsvTaProfileRepository(profileCsv),
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
        Path profileCsv = tempDir.resolve("ta_profile.csv");
        Path applicationCsv = tempDir.resolve("job_application.csv");

        JobPostingService jobPostingService = new JobPostingService(new CsvJobPostingRepository(jobCsv), fixedClock);
        TaProfileService profileService = new TaProfileService(new CsvTaProfileRepository(profileCsv));
        JobPosting posting = jobPostingService.postJob(new JobPostingDraft(
                "mo@g30.local",
                "Database TA",
                "EBU6201",
                "Support SQL workshops",
                "SQL,PostgreSQL",
                8,
                LocalDate.of(2026, 4, 15)
        ));
        profileService.saveProfile("ta@g30.local", new TaProfileDraft(
                "Alice Zhang",
                "231222001",
                "alice@g30.local",
                "MSc Software Engineering",
                "3.82",
                "SQL",
                "Weekdays after 2pm",
                ""
        ));

        JobApplicationService service = new JobApplicationService(
                new CsvJobApplicationRepository(applicationCsv),
                new CsvJobPostingRepository(jobCsv),
                new CsvTaProfileRepository(profileCsv),
                fixedClock
        );
        service.submitApplication("ta@g30.local", posting.jobId());

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> service.submitApplication("ta@g30.local", posting.jobId())
        );

        assertEquals("DUPLICATE_APPLICATION", error.getMessage());
    }

    @Test
    void shouldReturnApplicantsForMoOwnedJobAndAllowDecisionBeforeDeadline() throws Exception {
        Path tempDir = Files.createTempDirectory("job-application-decision");
        Path jobCsv = tempDir.resolve("job_posting.csv");
        Path profileCsv = tempDir.resolve("ta_profile.csv");
        Path applicationCsv = tempDir.resolve("job_application.csv");

        JobPostingService jobPostingService = new JobPostingService(new CsvJobPostingRepository(jobCsv), fixedClock);
        TaProfileService profileService = new TaProfileService(new CsvTaProfileRepository(profileCsv));
        JobPosting posting = jobPostingService.postJob(new JobPostingDraft(
                "mo@g30.local",
                "Software Engineering TA",
                "EBU6304",
                "Support labs and marking",
                "Java,JUnit,Scrum",
                10,
                LocalDate.of(2026, 4, 15)
        ));
        profileService.saveProfile("ta@g30.local", new TaProfileDraft(
                "Alice Zhang",
                "231222001",
                "alice@g30.local",
                "MSc Software Engineering",
                "3.82",
                "Java,Communication",
                "Weekdays after 2pm",
                ""
        ));
        profileService.attachCv("ta@g30.local", "C:/Users/alice/Documents/alice_cv.pdf");

        JobApplicationService service = new JobApplicationService(
                new CsvJobApplicationRepository(applicationCsv),
                new CsvJobPostingRepository(jobCsv),
                new CsvTaProfileRepository(profileCsv),
                fixedClock
        );
        JobApplication application = service.submitApplication("ta@g30.local", posting.jobId());

        List<MoApplicantView> applicants = service.listApplicantsForJob("mo@g30.local", posting.jobId());
        JobApplication reviewed = service.updateApplicationStatus("mo@g30.local", application.applicationId(), "ACCEPTED");

        assertEquals(1, applicants.size());
        assertEquals("Alice Zhang", applicants.getFirst().fullName());
        assertEquals("231222001", applicants.getFirst().studentId());
        assertEquals("Java,Communication", applicants.getFirst().skills());
        assertEquals("C:/Users/alice/Documents/alice_cv.pdf", applicants.getFirst().cvFilePath());
        assertEquals("ACCEPTED", reviewed.status());
        assertEquals("ACCEPTED", service.listApplicationsForTa("ta@g30.local").getFirst().status());
    }
}
