package com.group30.tarecruitment.jobs;

import com.group30.tarecruitment.applications.CsvJobApplicationRepository;
import com.group30.tarecruitment.applications.JobApplicationService;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void shouldSupportKeywordSkillAndModuleFiltersIncludingInvigilationJobs() throws Exception {
        Path tempDir = Files.createTempDirectory("job-posting-filter");
        Path jobCsv = tempDir.resolve("job_posting.csv");
        Path applicationCsv = tempDir.resolve("job_application.csv");
        JobPostingService service = new JobPostingService(
                new CsvJobPostingRepository(jobCsv),
                new CsvJobApplicationRepository(applicationCsv),
                fixedClock
        );

        service.postJob(new JobPostingDraft(
                "mo1@g30.local",
                "Software Engineering TA",
                "EBU6304",
                "Support labs",
                "Java,Scrum",
                10,
                LocalDate.of(2026, 4, 8)
        ));
        service.postJob(new JobPostingDraft(
                "admin@g30.local",
                "Final Exam Invigilation",
                "EBU6304",
                "Support final exam hall.",
                "",
                4,
                LocalDate.of(2026, 4, 12),
                "INVIGILATION",
                LocalDate.of(2026, 4, 20),
                "09:00-12:00",
                "Hall A",
                3
        ));

        List<JobPosting> keywordResults = service.browseOpenJobs("exam", "", "");
        List<JobPosting> skillResults = service.browseOpenJobs("", "java", "");
        List<JobPosting> moduleResults = service.browseOpenJobs("", "", "EBU6304");

        assertEquals(1, keywordResults.size());
        assertTrue(keywordResults.getFirst().isInvigilation());
        assertEquals(1, skillResults.size());
        assertEquals("Software Engineering TA", skillResults.getFirst().title());
        assertEquals(2, moduleResults.size());
    }

    @Test
    void shouldUpdatePostingAndBlockDeleteWhenAcceptedApplicationsExist() throws Exception {
        Path tempDir = Files.createTempDirectory("job-posting-edit-delete");
        Path jobCsv = tempDir.resolve("job_posting.csv");
        Path profileCsv = tempDir.resolve("ta_profile.csv");
        Path applicationCsv = tempDir.resolve("job_application.csv");

        JobPostingService jobService = new JobPostingService(
                new CsvJobPostingRepository(jobCsv),
                new CsvJobApplicationRepository(applicationCsv),
                fixedClock
        );
        TaProfileService profileService = new TaProfileService(new CsvTaProfileRepository(profileCsv));
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

        JobPosting posting = jobService.postJob(new JobPostingDraft(
                "mo@g30.local",
                "Software Engineering TA",
                "EBU6304",
                "Support labs and marking",
                "Java,JUnit,Scrum",
                10,
                LocalDate.of(2026, 4, 15)
        ));
        JobApplicationService applicationService = new JobApplicationService(
                new CsvJobApplicationRepository(applicationCsv),
                new CsvJobPostingRepository(jobCsv),
                new CsvTaProfileRepository(profileCsv),
                fixedClock
        );
        String applicationId = applicationService.submitApplication("ta@g30.local", posting.jobId()).applicationId();

        JobPosting updated = jobService.updatePosting("mo@g30.local", posting.jobId(), new JobPostingDraft(
                "mo@g30.local",
                "Final Exam Invigilation",
                "EBU6304",
                "Support final exam hall.",
                "",
                4,
                LocalDate.of(2026, 4, 18),
                "INVIGILATION",
                LocalDate.of(2026, 4, 20),
                "09:00-12:00",
                "Hall A",
                3
        ));

        assertTrue(updated.isInvigilation());
        assertEquals("Hall A", updated.location());

        jobService.deletePosting("mo@g30.local", posting.jobId());
        assertEquals(0, jobService.browseOpenJobs().size());

        JobPosting protectedPosting = jobService.postJob(new JobPostingDraft(
                "mo@g30.local",
                "Protected TA Role",
                "EBU6305",
                "Another posting",
                "Java",
                6,
                LocalDate.of(2026, 4, 20)
        ));
        String acceptedApplicationId = applicationService.submitApplication("ta@g30.local", protectedPosting.jobId()).applicationId();
        applicationService.updateApplicationStatus("mo@g30.local", acceptedApplicationId, "ACCEPTED");

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> jobService.deletePosting("mo@g30.local", protectedPosting.jobId())
        );

        assertEquals("JOB_HAS_ACCEPTED_APPLICATIONS", error.getMessage());
        assertEquals("ACCEPTED", applicationService.listApplicationsForTa("ta@g30.local").stream()
                .filter(summary -> summary.applicationId().equals(acceptedApplicationId))
                .findFirst()
                .orElseThrow()
                .status());
    }
}
