package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.applications.CsvJobApplicationRepository;
import com.group30.tarecruitment.applications.JobApplicationService;
import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPostingService;
import com.group30.tarecruitment.login.TaLoginResult;
import com.group30.tarecruitment.login.TaLoginService;
import com.group30.tarecruitment.matching.SkillMatchingService;
import com.group30.tarecruitment.notifications.CsvNotificationRepository;
import com.group30.tarecruitment.notifications.NotificationService;
import com.group30.tarecruitment.profile.CsvTaProfileRepository;
import com.group30.tarecruitment.profile.TaProfileService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import java.awt.Window;
import java.nio.file.Path;
import java.time.Clock;

public class TaLoginPanel extends JPanel {

    public TaLoginPanel(TaLoginService loginService) {
        setLayout(new GridLayout(3, 2, 8, 8));

        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("TA Login");

        add(new JLabel("Email"));
        add(emailField);
        add(new JLabel("Password"));
        add(passwordField);
        add(new JLabel(""));
        add(loginButton);

        loginButton.addActionListener(e -> {
            TaLoginResult result = loginService.login(
                    emailField.getText().trim(),
                    new String(passwordField.getPassword()),
                    "127.0.0.1"
            );
            if (result.success()) {
                Window currentWindow = SwingUtilities.getWindowAncestor(this);
                TaDashboardFrame dashboardFrame = new TaDashboardFrame(
                        emailField.getText().trim(),
                        result.sessionId(),
                        loginService,
                        new TaProfileService(new CsvTaProfileRepository(Path.of("data", "ta_profile.csv"))),
                        new JobPostingService(new CsvJobPostingRepository(Path.of("data", "job_posting.csv")), Clock.systemDefaultZone()),
                        new JobApplicationService(
                                new CsvJobApplicationRepository(Path.of("data", "job_application.csv")),
                                new CsvJobPostingRepository(Path.of("data", "job_posting.csv")),
                                new CsvTaProfileRepository(Path.of("data", "ta_profile.csv")),
                                new NotificationService(
                                        new CsvNotificationRepository(Path.of("data", "notifications.csv")),
                                        Clock.systemDefaultZone()
                                ),
                                Clock.systemDefaultZone()
                        ),
                        new SkillMatchingService(),
                        new NotificationService(
                                new CsvNotificationRepository(Path.of("data", "notifications.csv")),
                                Clock.systemDefaultZone()
                        ),
                        () -> {
                            JFrame loginFrame = new JFrame("TA Login");
                            loginFrame.setSize(460, 220);
                            loginFrame.setLocationRelativeTo(null);
                            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            loginFrame.setContentPane(new TaLoginPanel(loginService));
                            loginFrame.setVisible(true);
                        }
                );
                dashboardFrame.setVisible(true);
                if (currentWindow != null) {
                    currentWindow.dispose();
                }
                return;
            }
            JOptionPane.showMessageDialog(this, "Login failed: " + result.errorCode());
        });
    }
}
