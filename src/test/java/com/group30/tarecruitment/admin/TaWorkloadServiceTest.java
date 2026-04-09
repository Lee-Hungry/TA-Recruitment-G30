package com.group30.tarecruitment.admin;

import com.group30.tarecruitment.applications.CsvJobApplicationRepository;
import com.group30.tarecruitment.applications.JobApplicationService;
import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.jobs.JobPostingDraft;
import com.group30.tarecruitment.jobs.JobPostingService;
import com.group30.tarecruitment.profile.CsvTaProfileRepository;
import com.group30.tarecruitment.profile.TaProfileDraft;
import com.group30.tarecruitment.profile.TaProfileService;
import com.group30.tarecruitment.auth.repository.CsvUserAccountRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TaWorkloadServiceTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-29T08:00:00Z"), ZoneId.of("Asia/Shanghai"));

    @Test
    void shouldAggregateAcceptedHoursAndFlagThresholdBreachesFromSettingsFile() throws Exception {
        Path tempDir = Files.createTempDirectory("ta-workload");
        Path userCsv = tempDir.resolve("user_account.csv");
        Path jobCsv = tempDir.resolve("job_posting.csv");
        Path profileCsv = tempDir.resolve("ta_profile.csv");
        Path applicationCsv = tempDir.resolve("job_application.csv");
        Path settingsFile = tempDir.resolve("settings.properties");

        Files.writeString(
                userCsv,
                """
                user_id,email,password_hash,role,status,created_at,updated_at
                ta-001,ta1@g30.local,ta123456,TA,ACTIVE,2026-03-20T11:10:00+08:00,2026-03-20T11:10:00+08:00
                ta-002,ta2@g30.local,ta123456,TA,ACTIVE,2026-03-20T11:11:00+08:00,2026-03-20T11:11:00+08:00
                mo-001,mo@g30.local,mo123456,MO,ACTIVE,2026-03-20T11:20:00+08:00,2026-03-20T11:20:00+08:00
                """
        );
        Files.writeString(settingsFile, "max_weekly_hours=20" + System.lineSeparator());

        JobPostingService jobPostingService = new JobPostingService(new CsvJobPostingRepository(jobCsv), fixedClock);
        TaProfileService profileService = new TaProfileService(new CsvTaProfileRepository(profileCsv));
        JobApplicationService applicationService = new JobApplicationService(
                new CsvJobApplicationRepository(applicationCsv),
                new CsvJobPostingRepository(jobCsv),
                new CsvTaProfileRepository(profileCsv),
                fixedClock
        );

        profileService.saveProfile("ta1@g30.local", new TaProfileDraft(
                "Alice Zhang",
                "231222001",
                "alice@g30.local",
                "MSc Software Engineering",
                "3.80",
                "Java",
                "Monday",
                ""
        ));
        profileService.saveProfile("ta2@g30.local", new TaProfileDraft(
                "Bob Li",
                "231222002",
                "bob@g30.local",
                "MSc Software Engineering",
                "3.70",
                "Python",
                "Tuesday",
                ""
        ));

        JobPosting postingOne = jobPostingService.postJob(new JobPostingDraft(
                "mo@g30.local",
                "Software Engineering TA",
                "EBU6304",
                "Support labs and marking",
                "Java",
                12,
                LocalDate.of(2026, 4, 15)
        ));
        JobPosting postingTwo = jobPostingService.postJob(new JobPostingDraft(
                "mo@g30.local",
                "Data Structures TA",
                "EBU6208",
                "Support labs and marking",
                "Python",
                10,
                LocalDate.of(2026, 4, 18)
        ));

        String firstApplicationId = applicationService.submitApplication("ta1@g30.local", postingOne.jobId()).applicationId();
        String secondApplicationId = applicationService.submitApplication("ta1@g30.local", postingTwo.jobId()).applicationId();
        applicationService.submitApplication("ta2@g30.local", postingTwo.jobId());
        applicationService.updateApplicationStatus("mo@g30.local", firstApplicationId, "ACCEPTED");
        applicationService.updateApplicationStatus("mo@g30.local", secondApplicationId, "ACCEPTED");

        TaWorkloadService workloadService = new TaWorkloadService(
                new CsvUserAccountRepository(userCsv),
                new CsvTaProfileRepository(profileCsv),
                new CsvJobPostingRepository(jobCsv),
                new CsvJobApplicationRepository(applicationCsv),
                new WorkloadSettingsRepository(settingsFile)
        );

        List<TaWorkloadSummary> rows = workloadService.buildSummary();

        assertEquals(20, workloadService.maxWeeklyHours());
        assertEquals(2, rows.size());
        assertEquals("Alice Zhang", rows.getFirst().fullName());
        assertEquals(22, rows.getFirst().totalWeeklyHours());
        assertTrue(rows.getFirst().overloaded());
        assertEquals("Bob Li", rows.get(1).fullName());
        assertEquals(0, rows.get(1).totalWeeklyHours());
    }
}
