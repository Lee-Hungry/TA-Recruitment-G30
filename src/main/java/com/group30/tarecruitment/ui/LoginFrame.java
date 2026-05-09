package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.admin.AdminUserAccountService;
import com.group30.tarecruitment.admin.TaWorkloadService;
import com.group30.tarecruitment.admin.WorkloadSuggestionService;
import com.group30.tarecruitment.applications.JobApplicationService;
import com.group30.tarecruitment.auth.AuthResult;
import com.group30.tarecruitment.auth.AuthRole;
import com.group30.tarecruitment.auth.AuthService;
import com.group30.tarecruitment.jobs.JobPostingService;
import com.group30.tarecruitment.login.TaLoginResult;
import com.group30.tarecruitment.login.TaLoginService;
import com.group30.tarecruitment.matching.SkillMatchingService;
import com.group30.tarecruitment.mo.MoLoginResult;
import com.group30.tarecruitment.mo.MoLoginService;
import com.group30.tarecruitment.profile.TaProfileService;
import com.group30.tarecruitment.registration.TaRegistrationService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

public class LoginFrame extends JFrame {

    private static final Dimension LOGIN_CARD_SIZE = new Dimension(430, 520);

    private final AuthService authService;
    private final TaRegistrationService registrationService;
    private final TaLoginService taLoginService;
    private final MoLoginService moLoginService;
    private final TaProfileService profileService;
    private final JobPostingService jobPostingService;
    private final JobApplicationService applicationService;
    private final TaWorkloadService workloadService;
    private final WorkloadSuggestionService workloadSuggestionService;
    private final AdminUserAccountService userAccountService;
    private final SkillMatchingService skillMatchingService;
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final JComboBox<AuthRole> roleBox = new JComboBox<>(AuthRole.values());

    public LoginFrame(
            AuthService authService,
            TaRegistrationService registrationService,
            TaLoginService taLoginService,
            MoLoginService moLoginService,
            TaProfileService profileService,
            JobPostingService jobPostingService,
            JobApplicationService applicationService,
            TaWorkloadService workloadService,
            WorkloadSuggestionService workloadSuggestionService,
            AdminUserAccountService userAccountService,
            SkillMatchingService skillMatchingService
    ) {
        this.authService = authService;
        this.registrationService = registrationService;
        this.taLoginService = taLoginService;
        this.moLoginService = moLoginService;
        this.profileService = profileService;
        this.jobPostingService = jobPostingService;
        this.applicationService = applicationService;
        this.workloadService = workloadService;
        this.workloadSuggestionService = workloadSuggestionService;
        this.userAccountService = userAccountService;
        this.skillMatchingService = skillMatchingService;

        setTitle("TA Recruitment Login");
        setSize(1180, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(buildRootPanel());
    }

    private JPanel buildRootPanel() {
        JPanel root = new JPanel(new GridLayout(1, 2));
        root.setBackground(UiTheme.APP_BACKGROUND);
        root.add(buildWelcomePanel());
        root.add(buildLoginPanel());
        return root;
    }

    private JPanel buildWelcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UiTheme.APP_BACKGROUND);
        panel.setBorder(UiTheme.pagePadding());

        JLabel title = new JLabel("Welcome to TA Recruitment");
        title.setFont(UiTheme.TITLE_FONT);
        panel.add(title, BorderLayout.NORTH);

        JPanel placeholder = new JPanel(new BorderLayout());
        placeholder.setBackground(UiTheme.PANEL_BACKGROUND);
        placeholder.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.ACCENT_DARK, 2, true),
                BorderFactory.createEmptyBorder(24, 24, 24, 24)
        ));
        JLabel helper = new JLabel("<html><body style='width:340px'>"
                + "Register as a Teaching Assistant, sign in as an MO or Admin, and complete Sprint 1 workflows from a single login page."
                + "</body></html>");
        helper.setFont(UiTheme.SECTION_FONT);
        placeholder.add(helper, BorderLayout.NORTH);
        placeholder.add(new JLabel("<html><body style='width:360px'>"
                + "Use the seeded TA, MO, and Admin accounts from the CSV files, or open the registration form to create a new TA profile."
                + "</body></html>"), BorderLayout.CENTER);
        panel.add(placeholder, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildLoginPanel() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(UiTheme.APP_BACKGROUND);
        wrapper.setBorder(UiTheme.pagePadding());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UiTheme.PANEL_BACKGROUND);
        card.setBorder(UiTheme.cardBorder());
        card.setPreferredSize(loginCardPreferredSize());

        JLabel title = new JLabel("Log In");
        title.setFont(UiTheme.TITLE_FONT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("Access your university account");
        subtitle.setFont(UiTheme.BODY_FONT);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(28));

        UiTheme.styleInput(roleBox);
        UiTheme.styleInput(emailField);
        UiTheme.styleInput(passwordField);

        card.add(createFieldBlock("Select Role", roleBox));
        card.add(UiTheme.verticalGap(14));
        card.add(createFieldBlock("Email Address", emailField));
        card.add(UiTheme.verticalGap(14));
        card.add(createFieldBlock("Password", passwordField));
        card.add(UiTheme.verticalGap(24));

        JButton loginButton = UiTheme.primaryButton("Log in");
        loginButton.setAlignmentX(LEFT_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.FIELD_HEIGHT + 8));
        loginButton.addActionListener(e -> handleLogin());
        card.add(loginButton);
        card.add(Box.createVerticalStrut(12));

        JButton registerButton = UiTheme.secondaryButton("Register account");
        registerButton.setAlignmentX(LEFT_ALIGNMENT);
        registerButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.FIELD_HEIGHT + 8));
        registerButton.addActionListener(e -> openRegistrationWindow());
        card.add(registerButton);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 48));
        center.setOpaque(false);
        center.add(card);
        wrapper.add(center, BorderLayout.CENTER);
        return wrapper;
    }

    static Dimension loginCardPreferredSize() {
        return LOGIN_CARD_SIZE;
    }

    private JPanel createFieldBlock(String labelText, JComponent field) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        label.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT);

        block.add(label);
        block.add(Box.createVerticalStrut(6));
        block.add(field);
        return block;
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        AuthRole selectedRole = (AuthRole) roleBox.getSelectedItem();

        if (selectedRole == null) {
            JOptionPane.showMessageDialog(this, "Please select a role.");
            return;
        }

        switch (selectedRole) {
            case TA -> handleTaLogin(email, password);
            case MO -> handleMoLogin(email, password);
            case ADMIN -> handleAdminLogin(email, password);
            default -> throw new IllegalStateException("Unexpected role: " + selectedRole);
        }
    }

    private void handleTaLogin(String email, String password) {
        TaLoginResult result = taLoginService.login(email, password, "127.0.0.1");
        if (!result.success()) {
            JOptionPane.showMessageDialog(this, "TA login failed: " + result.errorCode());
            return;
        }

        setVisible(false);
        new TaDashboardFrame(
                email,
                result.sessionId(),
                taLoginService,
                profileService,
                jobPostingService,
                applicationService,
                skillMatchingService,
                this::showAgain
        ).setVisible(true);
    }

    private void handleMoLogin(String email, String password) {
        MoLoginResult result = moLoginService.login(email, password);
        if (!result.success()) {
            JOptionPane.showMessageDialog(this, "MO login failed: " + result.message());
            return;
        }
        if (!moLoginService.canAccessMoDashboard(result.sessionId(), result.sessionRole())) {
            JOptionPane.showMessageDialog(this, "MO access check failed.");
            return;
        }

        setVisible(false);
        new MoDashboardFrame(
                email,
                result.sessionId(),
                moLoginService,
                jobPostingService,
                applicationService,
                skillMatchingService,
                this::showAgain
        ).setVisible(true);
    }

    private void handleAdminLogin(String email, String password) {
        AuthResult result = authService.login(email, password, AuthRole.ADMIN, "127.0.0.1");
        if (!result.success()) {
            JOptionPane.showMessageDialog(this, "Admin login failed: " + result.error());
            return;
        }

        setVisible(false);
        new AdminDashboardFrame(
                email,
                workloadService,
                workloadSuggestionService,
                userAccountService,
                jobPostingService,
                this::showAgain
        ).setVisible(true);
    }

    private void openRegistrationWindow() {
        JFrame frame = new JFrame("TA Registration");
        frame.setSize(540, 420);
        frame.setLocationRelativeTo(this);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setContentPane(new TaRegistrationPanel(registrationService, frame::dispose));
        frame.setVisible(true);
    }

    private void showAgain() {
        passwordField.setText("");
        setVisible(true);
    }
}
