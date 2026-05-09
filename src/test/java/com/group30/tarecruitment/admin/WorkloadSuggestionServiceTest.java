package com.group30.tarecruitment.admin;

import com.group30.tarecruitment.ai.TestRecruitmentAiGateway;
import com.group30.tarecruitment.applications.CsvJobApplicationRepository;
import com.group30.tarecruitment.applications.JobApplicationService;
import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.jobs.JobPostingDraft;
import com.group30.tarecruitment.jobs.JobPostingService;
import com.group30.tarecruitment.matching.SkillMatchingService;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkloadSuggestionServiceTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-03-29T08:00:00Z"), ZoneId.of("Asia/Shanghai"));

    @Test
    void shouldRecommendPendingApplicantWhenAcceptedTaIsOverloaded() throws Exception {
        Path tempDir = Files.createTempDirectory("workload-suggestions");
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
                "3.82",
                "Java,JUnit",
                "Monday",
                ""
        ));
        profileService.saveProfile("ta2@g30.local", new TaProfileDraft(
                "Bob Li",
                "231222002",
                "bob@g30.local",
                "MSc Software Engineering",
                "3.76",
                "Java,JUnit,Scrum",
                "Tuesday",
                ""
        ));

        JobPosting overloadedJob = jobPostingService.postJob(new JobPostingDraft(
                "mo@g30.local",
                "Software Engineering TA",
                "EBU6304",
                "Support labs and marking",
                "Java,JUnit,Scrum",
                12,
                LocalDate.of(2026, 4, 15)
        ));
        JobPosting secondJob = jobPostingService.postJob(new JobPostingDraft(
                "mo@g30.local",
                "Database TA",
                "EBU6201",
                "Support SQL workshops",
                "SQL",
                10,
                LocalDate.of(2026, 4, 18)
        ));

        String acceptedOne = applicationService.submitApplication("ta1@g30.local", overloadedJob.jobId()).applicationId();
        String acceptedTwo = applicationService.submitApplication("ta1@g30.local", secondJob.jobId()).applicationId();
        applicationService.submitApplication("ta2@g30.local", overloadedJob.jobId());
        applicationService.updateApplicationStatus("mo@g30.local", acceptedOne, "ACCEPTED");
        applicationService.updateApplicationStatus("mo@g30.local", acceptedTwo, "ACCEPTED");

        TaWorkloadService workloadService = new TaWorkloadService(
                new CsvUserAccountRepository(userCsv),
                new CsvTaProfileRepository(profileCsv),
                new CsvJobPostingRepository(jobCsv),
                new CsvJobApplicationRepository(applicationCsv),
                new WorkloadSettingsRepository(settingsFile)
        );
        WorkloadSuggestionService suggestionService = new WorkloadSuggestionService(
                workloadService,
                new CsvJobPostingRepository(jobCsv),
                new CsvJobApplicationRepository(applicationCsv),
                new CsvTaProfileRepository(profileCsv),
                new SkillMatchingService(new TestRecruitmentAiGateway()),
                new TestRecruitmentAiGateway()
        );

        List<WorkloadSuggestion> suggestions = suggestionService.buildSuggestions();

        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.getFirst().title().contains("Bob Li"));
        assertTrue(suggestions.getFirst().detail().contains("would remain at 12 hrs/week"));
    }
}
