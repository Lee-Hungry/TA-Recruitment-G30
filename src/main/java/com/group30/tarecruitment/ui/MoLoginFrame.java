package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.applications.CsvJobApplicationRepository;
import com.group30.tarecruitment.applications.JobApplicationService;
import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPostingService;
import com.group30.tarecruitment.matching.SkillMatchingService;
import com.group30.tarecruitment.mo.MoLoginResult;
import com.group30.tarecruitment.mo.MoLoginService;
import com.group30.tarecruitment.notifications.CsvNotificationRepository;
import com.group30.tarecruitment.notifications.NotificationService;
import com.group30.tarecruitment.profile.CsvTaProfileRepository;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.time.Clock;

public class MoLoginFrame extends JFrame {

    public MoLoginFrame(MoLoginService loginService) {
        setTitle("MO Login");
        setSize(480, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("Login");

        panel.add(new JLabel("MO Email"));
        panel.add(emailField);
        panel.add(new JLabel("Password"));
        panel.add(passwordField);
        panel.add(new JLabel(""));
        panel.add(loginButton);

        loginButton.addActionListener(e -> {
            MoLoginResult result = loginService.login(emailField.getText().trim(), new String(passwordField.getPassword()));
            if (!result.success()) {
                JOptionPane.showMessageDialog(this, "MO login failed: " + result.message());
                return;
            }

            if (!loginService.canAccessMoDashboard(result.sessionId(), result.sessionRole())) {
                JOptionPane.showMessageDialog(this, "Route guard blocked access: role mismatch or session expired.");
                return;
            }

            new MoDashboardFrame(
                    emailField.getText().trim(),
                    result.sessionId(),
                    loginService,
                    new JobPostingService(
                            new CsvJobPostingRepository(Path.of("data", "job_posting.csv")),
                            new CsvJobApplicationRepository(Path.of("data", "job_application.csv")),
                            Clock.systemDefaultZone()
                    ),
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
                    }
            ).setVisible(true);
            dispose();
        });

        setContentPane(panel);
    }
}
