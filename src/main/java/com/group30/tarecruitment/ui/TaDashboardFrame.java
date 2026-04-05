package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.jobs.JobPostingService;
import com.group30.tarecruitment.login.TaLoginService;
import com.group30.tarecruitment.profile.CsvTaProfileRepository;
import com.group30.tarecruitment.profile.TaProfile;
import com.group30.tarecruitment.profile.TaProfileDraft;
import com.group30.tarecruitment.profile.TaProfileService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.time.Clock;

public class TaDashboardFrame extends JFrame {

    private static final String CARD_HOME = "HOME";
    private static final String CARD_PROFILE = "PROFILE";
    private static final String CARD_JOBS = "JOBS";

    private final String email;
    private final String sessionId;
    private final TaLoginService loginService;
    private final TaProfileService profileService;
    private final JobPostingService jobPostingService;
    private final Runnable showLoginFrame;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JLabel profileStatusValue = new JLabel();
    private final JLabel openJobsValue = new JLabel();
    private final JTextField fullNameField = new JTextField();
    private final JTextField studentIdField = new JTextField();
    private final JTextField contactEmailField = new JTextField();
    private final JTextField degreeField = new JTextField();
    private final JTextField gpaField = new JTextField();
    private final JTextArea skillsArea = new JTextArea(4, 20);
    private final JTextArea availabilityArea = new JTextArea(4, 20);
    private final DefaultListModel<JobPosting> jobListModel = new DefaultListModel<>();
    private final JList<JobPosting> jobList = new JList<>(jobListModel);
    private final JLabel jobTitleLabel = new JLabel("Select a job");
    private final JLabel jobMetaLabel = new JLabel("No job selected.");
    private final JTextArea jobDescriptionArea = new JTextArea();
    private final JTextArea jobSkillArea = new JTextArea();
    private boolean returningToLogin;

    public TaDashboardFrame(
            String email,
            String sessionId,
            TaLoginService loginService,
            TaProfileService profileService,
            JobPostingService jobPostingService,
            Runnable showLoginFrame
    ) {
        this.email = email == null ? "" : email.trim().toLowerCase();
        this.sessionId = sessionId;
        this.loginService = loginService;
        this.profileService = profileService;
        this.jobPostingService = jobPostingService;
        this.showLoginFrame = showLoginFrame;

        setTitle("TA Dashboard");
        setSize(1320, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                returnToLogin();
            }
        });

        UiTheme.styleTextArea(skillsArea);
        UiTheme.styleTextArea(availabilityArea);
        UiTheme.styleTextArea(jobDescriptionArea);
        UiTheme.styleTextArea(jobSkillArea);

        jobList.setCellRenderer(new JobRenderer());
        jobList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedJob(jobList.getSelectedValue());
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.APP_BACKGROUND);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMainArea(), BorderLayout.CENTER);
        setContentPane(root);

        UiTheme.styleInput(fullNameField);
        UiTheme.styleInput(studentIdField);
        UiTheme.styleInput(contactEmailField);
        UiTheme.styleInput(degreeField);
        UiTheme.styleInput(gpaField);
        jobDescriptionArea.setEditable(false);
        jobSkillArea.setEditable(false);

        loadProfile();
        refreshJobList();
        refreshDashboardSummary();
    }

    public TaDashboardFrame(String sessionId, TaLoginService loginService, Runnable showLoginFrame) {
        this(
                "",
                sessionId,
                loginService,
                new TaProfileService(new CsvTaProfileRepository(Path.of("data", "ta_profile.csv"))),
                new JobPostingService(new CsvJobPostingRepository(Path.of("data", "job_posting.csv")), Clock.systemDefaultZone()),
                showLoginFrame
        );
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, UiTheme.BORDER_COLOR),
                UiTheme.pagePadding()
        ));
        sidebar.setBackground(UiTheme.SIDEBAR_BACKGROUND);

        JLabel badge = new JLabel("TA");
        badge.setFont(UiTheme.SECTION_FONT);
        badge.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(badge);
        sidebar.add(Box.createVerticalStrut(16));

        JButton homeButton = UiTheme.navButton("Dashboard");
        homeButton.addActionListener(e -> cardLayout.show(contentPanel, CARD_HOME));
        sidebar.add(homeButton);

        JButton profileButton = UiTheme.navButton("Profile");
        profileButton.addActionListener(e -> {
            loadProfile();
            cardLayout.show(contentPanel, CARD_PROFILE);
        });
        sidebar.add(profileButton);

        JButton jobsButton = UiTheme.navButton("Find Jobs");
        jobsButton.addActionListener(e -> {
            refreshJobList();
            cardLayout.show(contentPanel, CARD_JOBS);
        });
        sidebar.add(jobsButton);

        JButton applicationsButton = UiTheme.navButton("My Applications");
        applicationsButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Applications arrive in Sprint 2."));
        sidebar.add(applicationsButton);

        JButton messagesButton = UiTheme.navButton("Messages");
        messagesButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Messages arrive in Sprint 2."));
        sidebar.add(messagesButton);

        sidebar.add(Box.createVerticalGlue());
        JButton logoutButton = UiTheme.secondaryButton("Logout");
        logoutButton.addActionListener(e -> handleLogout());
        sidebar.add(logoutButton);
        return sidebar;
    }

    private JPanel createMainArea() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(UiTheme.APP_BACKGROUND);
        panel.setBorder(UiTheme.pagePadding());
        panel.add(createTopBar(), BorderLayout.NORTH);

        contentPanel.setOpaque(false);
        contentPanel.add(createHomeCard(), CARD_HOME);
        contentPanel.add(createProfileCard(), CARD_PROFILE);
        contentPanel.add(createJobsCard(), CARD_JOBS);
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setBackground(UiTheme.APP_BACKGROUND);
        JTextField searchField = new JTextField("Search jobs, skills...");
        UiTheme.styleInput(searchField);
        topBar.add(searchField, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        right.add(new JLabel("Notifications"));
        right.add(new JLabel(email.isBlank() ? "TA User" : email));
        topBar.add(right, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createHomeCard() {
        JPanel card = new JPanel(new GridLayout(1, 2, 18, 18));
        card.setOpaque(false);
        card.add(buildMetricCard("Profile Status", profileStatusValue));
        card.add(buildMetricCard("Open Jobs", openJobsValue));
        return card;
    }

    private JPanel buildMetricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBackground(UiTheme.PANEL_BACKGROUND);
        card.setBorder(UiTheme.cardBorder());
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UiTheme.SECTION_FONT);
        valueLabel.setFont(UiTheme.TITLE_FONT);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JPanel createProfileCard() {
        JPanel card = new JPanel(new BorderLayout(18, 18));
        card.setBackground(UiTheme.PANEL_BACKGROUND);
        card.setBorder(UiTheme.cardBorder());

        JLabel title = new JLabel("Edit Applicant Profile");
        title.setFont(UiTheme.SECTION_FONT);
        card.add(title, BorderLayout.NORTH);

        JPanel columns = new JPanel(new GridLayout(1, 2, 18, 18));
        columns.setOpaque(false);

        JPanel basics = UiTheme.transparentPanel();
        basics.setLayout(new BoxLayout(basics, BoxLayout.Y_AXIS));
        basics.add(createFieldBlock("Full Name", fullNameField));
        basics.add(UiTheme.verticalGap(14));
        basics.add(createFieldBlock("Student ID", studentIdField));
        basics.add(UiTheme.verticalGap(14));
        basics.add(createFieldBlock("Contact Email", contactEmailField));
        basics.add(UiTheme.verticalGap(14));
        basics.add(createFieldBlock("Degree Programme", degreeField));
        basics.add(UiTheme.verticalGap(14));
        basics.add(createFieldBlock("GPA", gpaField));

        JPanel details = UiTheme.transparentPanel();
        details.setLayout(new BoxLayout(details, BoxLayout.Y_AXIS));
        details.add(createTextAreaBlock("Skills", skillsArea));
        details.add(UiTheme.verticalGap(14));
        details.add(createTextAreaBlock("Availability", availabilityArea));

        columns.add(basics);
        columns.add(details);
        card.add(columns, BorderLayout.CENTER);

        JButton saveButton = UiTheme.primaryButton("Save Profile");
        saveButton.addActionListener(e -> saveProfile());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(saveButton);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createJobsCard() {
        JPanel left = new JPanel(new BorderLayout(12, 12));
        left.setBackground(UiTheme.PANEL_BACKGROUND);
        left.setBorder(UiTheme.cardBorder());
        JLabel leftTitle = new JLabel("Available TA Jobs");
        leftTitle.setFont(UiTheme.SECTION_FONT);
        left.add(leftTitle, BorderLayout.NORTH);
        left.add(new JScrollPane(jobList), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(12, 12));
        right.setBackground(UiTheme.PANEL_BACKGROUND);
        right.setBorder(UiTheme.cardBorder());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        jobTitleLabel.setFont(UiTheme.SECTION_FONT);
        header.add(jobTitleLabel, BorderLayout.WEST);
        JButton disabledApply = UiTheme.secondaryButton("Apply in Sprint 2");
        disabledApply.setEnabled(false);
        header.add(disabledApply, BorderLayout.EAST);
        right.add(header, BorderLayout.NORTH);

        JPanel details = new JPanel(new GridLayout(1, 2, 12, 12));
        details.setOpaque(false);

        JPanel descriptionCard = new JPanel(new BorderLayout(8, 8));
        descriptionCard.setOpaque(false);
        JLabel descriptionLabel = new JLabel("Job Overview");
        descriptionLabel.setFont(UiTheme.SECTION_FONT);
        descriptionCard.add(descriptionLabel, BorderLayout.NORTH);
        JPanel descriptionContent = UiTheme.transparentPanel();
        descriptionContent.setLayout(new BorderLayout(8, 8));
        descriptionContent.add(jobMetaLabel, BorderLayout.NORTH);
        descriptionContent.add(new JScrollPane(jobDescriptionArea), BorderLayout.CENTER);
        descriptionCard.add(descriptionContent, BorderLayout.CENTER);

        JPanel skillCard = new JPanel(new BorderLayout(8, 8));
        skillCard.setOpaque(false);
        JLabel skillTitle = new JLabel("Required Skills");
        skillTitle.setFont(UiTheme.SECTION_FONT);
        skillCard.add(skillTitle, BorderLayout.NORTH);
        skillCard.add(new JScrollPane(jobSkillArea), BorderLayout.CENTER);

        details.add(descriptionCard);
        details.add(skillCard);
        right.add(details, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setResizeWeight(0.38);
        splitPane.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(splitPane, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadProfile() {
        TaProfile profile = profileService.loadProfile(email);
        fullNameField.setText(profile.fullName());
        studentIdField.setText(profile.studentId());
        contactEmailField.setText(profile.contactEmail());
        degreeField.setText(profile.degreeProgramme());
        gpaField.setText(profile.gpa());
        skillsArea.setText(profile.skills());
        availabilityArea.setText(profile.availability());
    }

    private void saveProfile() {
        try {
            profileService.saveProfile(email, new TaProfileDraft(
                    fullNameField.getText(),
                    studentIdField.getText(),
                    contactEmailField.getText(),
                    degreeField.getText(),
                    gpaField.getText(),
                    skillsArea.getText(),
                    availabilityArea.getText(),
                    ""
            ));
            refreshDashboardSummary();
            JOptionPane.showMessageDialog(this, "Profile saved.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Profile save failed: " + ex.getMessage());
        }
    }

    private void refreshJobList() {
        jobListModel.clear();
        for (JobPosting posting : jobPostingService.browseOpenJobs()) {
            jobListModel.addElement(posting);
        }
        if (!jobListModel.isEmpty()) {
            jobList.setSelectedIndex(0);
        } else {
            showSelectedJob(null);
        }
    }

    private void showSelectedJob(JobPosting job) {
        if (job == null) {
            jobTitleLabel.setText("Select a job");
            jobMetaLabel.setText("No open jobs available.");
            jobDescriptionArea.setText("");
            jobSkillArea.setText("");
            return;
        }
        jobTitleLabel.setText(job.moduleCode() + ": " + job.title());
        jobMetaLabel.setText("Hours " + job.hoursPerWeek() + " hrs/week | Deadline " + job.applicationDeadline());
        jobDescriptionArea.setText(job.description());
        jobSkillArea.setText(job.requiredSkills());
    }

    private void refreshDashboardSummary() {
        TaProfile profile = profileService.loadProfile(email);
        profileStatusValue.setText(profile.fullName().isBlank() ? "Incomplete" : "Ready");
        openJobsValue.setText(Integer.toString(jobPostingService.browseOpenJobs().size()));
    }

    private void handleLogout() {
        boolean revoked = loginService.logout(sessionId);
        if (!revoked) {
            JOptionPane.showMessageDialog(this, "Logout failed: session not found.");
            return;
        }
        returnToLogin();
    }

    private void returnToLogin() {
        if (returningToLogin) {
            return;
        }
        returningToLogin = true;
        dispose();
        showLoginFrame.run();
    }

    private JPanel createFieldBlock(String labelText, JTextField field) {
        JPanel block = UiTheme.transparentPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        label.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT);
        block.add(label);
        block.add(Box.createVerticalStrut(6));
        block.add(field);
        return block;
    }

    private JPanel createTextAreaBlock(String labelText, JTextArea area) {
        JPanel block = UiTheme.transparentPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        label.setAlignmentX(LEFT_ALIGNMENT);
        JScrollPane scrollPane = UiTheme.createTextAreaScrollPane(area);
        scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        block.add(label);
        block.add(Box.createVerticalStrut(6));
        block.add(scrollPane);
        return block;
    }

    private static final class JobRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList<?> list,
                Object value,
                int index,
                boolean isSelected,
                boolean cellHasFocus
        ) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof JobPosting job) {
                label.setText("<html><b>" + job.title() + "</b><br/>"
                        + job.moduleCode() + " | " + job.hoursPerWeek() + " hrs/week | Deadline " + job.applicationDeadline()
                        + "</html>");
                label.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            }
            return label;
        }
    }
}
