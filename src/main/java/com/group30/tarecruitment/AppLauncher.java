package com.group30.tarecruitment;

import com.group30.tarecruitment.applications.CsvJobApplicationRepository;
import com.group30.tarecruitment.applications.JobApplicationService;
import com.group30.tarecruitment.auth.AuthService;
import com.group30.tarecruitment.auth.repository.CsvSessionTokenRepository;
import com.group30.tarecruitment.auth.repository.CsvUserAccountRepository;
import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPostingService;
import com.group30.tarecruitment.login.CsvUserCredentialRepository;
import com.group30.tarecruitment.login.TaLoginService;
import com.group30.tarecruitment.mo.CsvMoAccountRepository;
import com.group30.tarecruitment.mo.CsvSessionRepository;
import com.group30.tarecruitment.mo.MoLoginService;
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

            TaProfileService profileService = new TaProfileService(new CsvTaProfileRepository(profileCsv));
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
                    new CsvUserAccountRepository(userCsv),
                    new CsvSessionTokenRepository(sessionCsv)
            );
            JobPostingService jobPostingService = new JobPostingService(
                    new CsvJobPostingRepository(jobCsv),
                    Clock.systemDefaultZone()
            );
            JobApplicationService applicationService = new JobApplicationService(
                    new CsvJobApplicationRepository(applicationCsv),
                    new CsvJobPostingRepository(jobCsv),
                    new CsvTaProfileRepository(profileCsv),
                    Clock.systemDefaultZone()
            );

            LoginFrame frame = new LoginFrame(
                    authService,
                    registrationService,
                    taLoginService,
                    moLoginService,
                    profileService,
                    jobPostingService,
                    applicationService
            );
            frame.setVisible(true);
        });
    }
}
