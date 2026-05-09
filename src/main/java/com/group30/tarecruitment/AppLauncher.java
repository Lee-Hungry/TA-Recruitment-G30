package com.group30.tarecruitment;

import com.group30.tarecruitment.admin.AdminUserAccountService;
import com.group30.tarecruitment.admin.TaWorkloadService;
import com.group30.tarecruitment.admin.WorkloadSettingsRepository;
import com.group30.tarecruitment.admin.WorkloadSuggestionService;
import com.group30.tarecruitment.applications.CsvJobApplicationRepository;
import com.group30.tarecruitment.applications.JobApplicationService;
import com.group30.tarecruitment.auth.AuthService;
import com.group30.tarecruitment.auth.repository.CsvSessionTokenRepository;
import com.group30.tarecruitment.auth.repository.CsvUserAccountRepository;
import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPostingService;
import com.group30.tarecruitment.login.CsvUserCredentialRepository;
import com.group30.tarecruitment.login.TaLoginService;
import com.group30.tarecruitment.matching.SkillMatchingService;
import com.group30.tarecruitment.mo.CsvMoAccountRepository;
import com.group30.tarecruitment.mo.CsvSessionRepository;
import com.group30.tarecruitment.mo.MoLoginService;
import com.group30.tarecruitment.notifications.CsvNotificationRepository;
import com.group30.tarecruitment.notifications.NotificationService;
import com.group30.tarecruitment.profile.CsvTaProfileRepository;
import com.group30.tarecruitment.profile.TaProfileService;
import com.group30.tarecruitment.registration.CsvUserRepository;
import com.group30.tarecruitment.registration.TaRegistrationService;
import com.group30.tarecruitment.ui.LoginFrame;

import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.time.Clock;

public class AppLauncher {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Path userCsv = Path.of("data", "user_account.csv");
            Path sessionCsv = Path.of("data", "session_token.csv");
            Path moSessionCsv = Path.of("data", "mo_session.csv");
            Path profileCsv = Path.of("data", "ta_profile.csv");
            Path jobCsv = Path.of("data", "job_posting.csv");
            Path applicationCsv = Path.of("data", "job_application.csv");
            Path notificationCsv = Path.of("data", "notifications.csv");
            Path settingsFile = Path.of("data", "settings.properties");

            CsvUserAccountRepository userAccountRepository = new CsvUserAccountRepository(userCsv);
            CsvTaProfileRepository profileRepository = new CsvTaProfileRepository(profileCsv);
            CsvJobPostingRepository jobRepository = new CsvJobPostingRepository(jobCsv);
            CsvJobApplicationRepository applicationRepository = new CsvJobApplicationRepository(applicationCsv);
            NotificationService notificationService = new NotificationService(
                    new CsvNotificationRepository(notificationCsv),
                    Clock.systemDefaultZone()
            );
            SkillMatchingService skillMatchingService = new SkillMatchingService();

            TaProfileService profileService = new TaProfileService(profileRepository);
            TaRegistrationService registrationService = new TaRegistrationService(
                    new CsvUserRepository(userCsv),
                    profileService
            );
            TaLoginService taLoginService = new TaLoginService(
                    new CsvUserCredentialRepository(userCsv),
                    new com.group30.tarecruitment.login.CsvSessionTokenRepository(sessionCsv)
            );
            MoLoginService moLoginService = new MoLoginService(
                    new CsvMoAccountRepository(userCsv),
                    new CsvSessionRepository(moSessionCsv)
            );
            AuthService authService = new AuthService(
                    userAccountRepository,
                    new CsvSessionTokenRepository(sessionCsv)
            );
            JobPostingService jobPostingService = new JobPostingService(
                    jobRepository,
                    applicationRepository,
                    Clock.systemDefaultZone()
            );
            JobApplicationService applicationService = new JobApplicationService(
                    applicationRepository,
                    jobRepository,
                    profileRepository,
                    notificationService,
                    Clock.systemDefaultZone()
            );
            TaWorkloadService workloadService = new TaWorkloadService(
                    userAccountRepository,
                    profileRepository,
                    jobRepository,
                    applicationRepository,
                    new WorkloadSettingsRepository(settingsFile)
            );
            AdminUserAccountService userAccountService = new AdminUserAccountService(
                    userAccountRepository,
                    profileRepository,
                    Clock.systemDefaultZone()
            );
            WorkloadSuggestionService workloadSuggestionService = new WorkloadSuggestionService(
                    workloadService,
                    jobRepository,
                    applicationRepository,
                    profileRepository,
                    skillMatchingService
            );

            LoginFrame frame = new LoginFrame(
                    authService,
                    registrationService,
                    taLoginService,
                    moLoginService,
                    profileService,
                    jobPostingService,
                    applicationService,
                    workloadService,
                    workloadSuggestionService,
                    userAccountService,
                    skillMatchingService,
                    notificationService
            );
            frame.setVisible(true);
        });
    }
}
