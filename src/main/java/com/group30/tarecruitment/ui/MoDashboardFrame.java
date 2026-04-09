package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.applications.CsvJobApplicationRepository;
import com.group30.tarecruitment.applications.JobApplicationService;
import com.group30.tarecruitment.applications.MoApplicantView;
import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.jobs.JobPostingDraft;
import com.group30.tarecruitment.jobs.JobPostingService;
import com.group30.tarecruitment.mo.CsvMoAccountRepository;
import com.group30.tarecruitment.mo.CsvSessionRepository;
import com.group30.tarecruitment.mo.MoLoginService;
import com.group30.tarecruitment.profile.CsvTaProfileRepository;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MoDashboardFrame extends JFrame {

    private static final String CARD_HOME = "HOME";
    private static final String CARD_POST = "POST";
    private static final String CARD_POSTINGS = "POSTINGS";
    private static final String CARD_APPLICANTS = "APPLICANTS";

    private final String moEmail;
    private final String sessionId;
    private final MoLoginService loginService;
    private final JobPostingService jobPostingService;
    private final JobApplicationService applicationService;
    private final Runnable showLoginFrame;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final DefaultTableModel postingsModel = new DefaultTableModel(
            new Object[]{"Job Title", "Module", "Deadline", "Hours", "Stage"},
            0
    );
    private final JLabel totalPostingsValue = new JLabel();
    private final JLabel openPostingsValue = new JLabel();
    private final JLabel pendingApplicantsValue = new JLabel();
    private final JTextField jobTitleField = new JTextField();
    private final JTextField moduleCodeField = new JTextField();
    private final JTextField hoursField = new JTextField();
    private final JTextField deadlineField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(6, 20);
    private final JTextArea skillsArea = new JTextArea(4, 20);
    private final DefaultTableModel applicantsModel = new DefaultTableModel(
            new Object[]{"Applicant", "Student ID", "Skills", "Applied", "Status"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable applicantsTable = new JTable(applicantsModel);
    private final JComboBox<JobPosting> applicantJobSelector = new JComboBox<>();
    private final JLabel applicantNameLabel = new JLabel("Select an applicant");
    private final JLabel applicantMetaLabel = new JLabel("No applicant selected.");
    private final JLabel applicantCvLabel = new JLabel("CV path: -");
    private final JTextArea applicantProfileArea = buildReadonlyTextArea();
    private final List<MoApplicantView> currentApplicants = new ArrayList<>();
    private boolean returningToLogin;

    public MoDashboardFrame(
            String moEmail,
            String sessionId,
            MoLoginService loginService,
            JobPostingService jobPostingService,
            JobApplicationService applicationService,
            Runnable showLoginFrame
    ) {
        this.moEmail = moEmail == null ? "" : moEmail.trim().toLowerCase();
        this.sessionId = sessionId;
        this.loginService = loginService;
        this.jobPostingService = jobPostingService;
        this.applicationService = applicationService;
        this.showLoginFrame = showLoginFrame;

        setTitle("MO Dashboard");
        setSize(1320, 780);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                returnToLogin();
            }
        });

        UiTheme.styleTextArea(descriptionArea);
        UiTheme.styleTextArea(skillsArea);
        UiTheme.styleInput(jobTitleField);
        UiTheme.styleInput(moduleCodeField);
        UiTheme.styleInput(hoursField);
        UiTheme.styleInput(deadlineField);
        UiTheme.styleInput(applicantJobSelector);

        applicantJobSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    javax.swing.JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof JobPosting posting) {
                    label.setText(posting.moduleCode() + " - " + posting.title());
                }
                return label;
            }
        });
        applicantJobSelector.addActionListener(e -> refreshApplicantsForSelectedJob());
        applicantsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        applicantsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = applicantsTable.getSelectedRow();
                showSelectedApplicant(row >= 0 && row < currentApplicants.size() ? currentApplicants.get(row) : null);
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.APP_BACKGROUND);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMainArea(), BorderLayout.CENTER);
        setContentPane(root);

        refreshMyPostings();
        refreshApplicantJobOptions();
    }

    public MoDashboardFrame() {
        this(
                "",
                "",
                new MoLoginService(
                        new CsvMoAccountRepository(Path.of("data", "user_account.csv")),
                        new CsvSessionRepository(Path.of("data", "session_token.csv"))
                ),
                new JobPostingService(new CsvJobPostingRepository(Path.of("data", "job_posting.csv")), Clock.systemDefaultZone()),
                new JobApplicationService(
                        new CsvJobApplicationRepository(Path.of("data", "job_application.csv")),
                        new CsvJobPostingRepository(Path.of("data", "job_posting.csv")),
                        new CsvTaProfileRepository(Path.of("data", "ta_profile.csv")),
                        Clock.systemDefaultZone()
                ),
                () -> {
                }
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

        JLabel badge = new JLabel("MO");
        badge.setFont(UiTheme.SECTION_FONT);
        badge.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(badge);
        sidebar.add(Box.createVerticalStrut(16));

        JButton homeButton = UiTheme.navButton("Dashboard");
        homeButton.addActionListener(e -> {
            refreshMyPostings();
            cardLayout.show(contentPanel, CARD_HOME);
        });
        sidebar.add(homeButton);

        JButton postButton = UiTheme.navButton("Job Posting");
        postButton.addActionListener(e -> cardLayout.show(contentPanel, CARD_POST));
        sidebar.add(postButton);

        JButton postingsButton = UiTheme.navButton("My Postings");
        postingsButton.addActionListener(e -> {
            refreshMyPostings();
            cardLayout.show(contentPanel, CARD_POSTINGS);
        });
        sidebar.add(postingsButton);

        JButton applicantsButton = UiTheme.navButton("Applicants");
        applicantsButton.addActionListener(e -> {
            refreshApplicantJobOptions();
            cardLayout.show(contentPanel, CARD_APPLICANTS);
        });
        sidebar.add(applicantsButton);

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
        contentPanel.add(createDashboardCard(), CARD_HOME);
        contentPanel.add(createPostingCard(), CARD_POST);
        contentPanel.add(createMyPostingsCard(), CARD_POSTINGS);
        contentPanel.add(createApplicantsCard(), CARD_APPLICANTS);
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setBackground(UiTheme.APP_BACKGROUND);
        JTextField searchField = new JTextField("Search postings, applicants...");
        UiTheme.styleInput(searchField);
        topBar.add(searchField, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        right.add(new JLabel("Notifications"));
        right.add(new JLabel(moEmail.isBlank() ? "MO User" : moEmail));
        topBar.add(right, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createDashboardCard() {
        JPanel card = new JPanel(new GridLayout(1, 3, 18, 18));
        card.setOpaque(false);
        card.add(buildMetricCard("Total Postings", totalPostingsValue));
        card.add(buildMetricCard("Open Postings", openPostingsValue));
        card.add(buildMetricCard("Pending Applicants", pendingApplicantsValue));
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

    private JPanel createPostingCard() {
        JPanel card = new JPanel(new BorderLayout(18, 18));
        card.setBackground(UiTheme.PANEL_BACKGROUND);
        card.setBorder(UiTheme.cardBorder());

        JLabel title = new JLabel("Post a new job");
        title.setFont(UiTheme.TITLE_FONT);
        card.add(title, BorderLayout.NORTH);

        JPanel basicsPanel = UiTheme.transparentPanel();
        basicsPanel.setLayout(new BoxLayout(basicsPanel, BoxLayout.Y_AXIS));
        basicsPanel.add(createFieldBlock("Job Title", jobTitleField));
        basicsPanel.add(UiTheme.verticalGap(14));
        basicsPanel.add(createFieldBlock("Module Code", moduleCodeField));
        basicsPanel.add(UiTheme.verticalGap(14));
        basicsPanel.add(createFieldBlock("Hours per week", hoursField));
        basicsPanel.add(UiTheme.verticalGap(14));
        basicsPanel.add(createFieldBlock("Application Deadline (yyyy-MM-dd)", deadlineField));

        JPanel descriptionPanel = UiTheme.transparentPanel();
        descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.Y_AXIS));
        descriptionPanel.add(createTextAreaBlock("Required Skills", skillsArea));
        descriptionPanel.add(UiTheme.verticalGap(14));
        descriptionPanel.add(createTextAreaBlock("Job Description", descriptionArea));

        JPanel wrapper = new JPanel(new GridLayout(1, 2, 18, 18));
        wrapper.setOpaque(false);
        wrapper.add(basicsPanel);
        wrapper.add(descriptionPanel);
        card.add(wrapper, BorderLayout.CENTER);

        JButton submitButton = UiTheme.primaryButton("Publish Job");
        submitButton.addActionListener(e -> postJob());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        footer.add(submitButton);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createMyPostingsCard() {
        JPanel card = new JPanel(new BorderLayout(18, 18));
        card.setBackground(UiTheme.PANEL_BACKGROUND);
        card.setBorder(UiTheme.cardBorder());
        JLabel title = new JLabel("My postings");
        title.setFont(UiTheme.TITLE_FONT);
        card.add(title, BorderLayout.NORTH);
        card.add(new JScrollPane(new JTable(postingsModel)), BorderLayout.CENTER);
        return card;
    }

    private JPanel createApplicantsCard() {
        JPanel left = new JPanel(new BorderLayout(12, 12));
        left.setBackground(UiTheme.PANEL_BACKGROUND);
        left.setBorder(UiTheme.cardBorder());

        JPanel leftHeader = new JPanel(new BorderLayout(12, 12));
        leftHeader.setOpaque(false);
        JLabel title = new JLabel("Applicants by posting");
        title.setFont(UiTheme.SECTION_FONT);
        leftHeader.add(title, BorderLayout.WEST);
        leftHeader.add(applicantJobSelector, BorderLayout.EAST);
        left.add(leftHeader, BorderLayout.NORTH);
        left.add(new JScrollPane(applicantsTable), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(12, 12));
        right.setBackground(UiTheme.PANEL_BACKGROUND);
        right.setBorder(UiTheme.cardBorder());
        applicantNameLabel.setFont(UiTheme.SECTION_FONT);
        right.add(applicantNameLabel, BorderLayout.NORTH);

        JPanel detailBody = new JPanel(new BorderLayout(8, 8));
        detailBody.setOpaque(false);
        detailBody.add(applicantMetaLabel, BorderLayout.NORTH);
        detailBody.add(new JScrollPane(applicantProfileArea), BorderLayout.CENTER);
        detailBody.add(applicantCvLabel, BorderLayout.SOUTH);
        right.add(detailBody, BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setResizeWeight(0.52);
        splitPane.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(splitPane, BorderLayout.CENTER);
        return wrapper;
    }

    private void postJob() {
        try {
            jobPostingService.postJob(new JobPostingDraft(
                    moEmail,
                    jobTitleField.getText(),
                    moduleCodeField.getText(),
                    descriptionArea.getText(),
                    skillsArea.getText(),
                    Integer.parseInt(hoursField.getText().trim()),
                    LocalDate.parse(deadlineField.getText().trim())
            ));
            clearForm();
            refreshMyPostings();
            refreshApplicantJobOptions();
            cardLayout.show(contentPanel, CARD_POSTINGS);
            JOptionPane.showMessageDialog(this, "Job posted successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Posting failed: " + ex.getMessage());
        }
    }

    private void clearForm() {
        jobTitleField.setText("");
        moduleCodeField.setText("");
        hoursField.setText("");
        deadlineField.setText("");
        descriptionArea.setText("");
        skillsArea.setText("");
    }

    private void refreshMyPostings() {
        postingsModel.setRowCount(0);
        List<JobPosting> postings = jobPostingService.viewPostingsByMo(moEmail);
        int openCount = 0;
        for (JobPosting posting : postings) {
            postingsModel.addRow(new Object[]{
                    posting.title(),
                    posting.moduleCode(),
                    posting.applicationDeadline(),
                    posting.hoursPerWeek(),
                    posting.status()
            });
            if ("OPEN".equalsIgnoreCase(posting.status())) {
                openCount++;
            }
        }
        totalPostingsValue.setText(Integer.toString(postings.size()));
        openPostingsValue.setText(Integer.toString(openCount));
        pendingApplicantsValue.setText(Integer.toString(countPendingApplicants(postings)));
    }

    private int countPendingApplicants(List<JobPosting> postings) {
        int count = 0;
        for (JobPosting posting : postings) {
            count += applicationService.listApplicantsForJob(moEmail, posting.jobId()).stream()
                    .filter(applicant -> "PENDING".equalsIgnoreCase(applicant.status()))
                    .count();
        }
        return count;
    }

    private void refreshApplicantJobOptions() {
        applicantJobSelector.removeAllItems();
        List<JobPosting> postings = jobPostingService.viewPostingsByMo(moEmail);
        for (JobPosting posting : postings) {
            applicantJobSelector.addItem(posting);
        }
        if (applicantJobSelector.getItemCount() > 0) {
            applicantJobSelector.setSelectedIndex(0);
            refreshApplicantsForSelectedJob();
        } else {
            applicantsModel.setRowCount(0);
            currentApplicants.clear();
            showSelectedApplicant(null);
        }
        refreshMyPostings();
    }

    private void refreshApplicantsForSelectedJob() {
        JobPosting selectedJob = (JobPosting) applicantJobSelector.getSelectedItem();
        applicantsModel.setRowCount(0);
        currentApplicants.clear();
        if (selectedJob == null) {
            showSelectedApplicant(null);
            return;
        }

        currentApplicants.addAll(applicationService.listApplicantsForJob(moEmail, selectedJob.jobId()));
        for (MoApplicantView applicant : currentApplicants) {
            applicantsModel.addRow(new Object[]{
                    applicant.fullName(),
                    applicant.studentId(),
                    applicant.skills(),
                    applicant.appliedAt(),
                    applicant.status()
            });
        }
        if (!currentApplicants.isEmpty()) {
            applicantsTable.setRowSelectionInterval(0, 0);
        } else {
            showSelectedApplicant(null);
        }
    }

    private void showSelectedApplicant(MoApplicantView applicant) {
        if (applicant == null) {
            applicantNameLabel.setText("Select an applicant");
            applicantMetaLabel.setText("No applicant selected.");
            applicantCvLabel.setText("CV path: -");
            applicantProfileArea.setText("Choose a posting and an applicant to review the full TA profile and uploaded CV path.");
            return;
        }

        applicantNameLabel.setText(applicant.fullName());
        applicantMetaLabel.setText(applicant.moduleCode() + " | Applied " + applicant.appliedAt() + " | Status " + applicant.status());
        applicantCvLabel.setText("CV path: " + (applicant.cvFilePath().isBlank() ? "No CV uploaded" : applicant.cvFilePath()));
        applicantProfileArea.setText(
                "Student ID: " + applicant.studentId() + System.lineSeparator()
                        + "Email: " + applicant.taEmail() + System.lineSeparator()
                        + "Degree: " + blankFallback(applicant.degreeProgramme()) + System.lineSeparator()
                        + "GPA: " + blankFallback(applicant.gpa()) + System.lineSeparator()
                        + "Weekly hours for this role: " + applicant.hoursPerWeek() + System.lineSeparator()
                        + System.lineSeparator()
                        + "Skills" + System.lineSeparator()
                        + blankFallback(applicant.skills()) + System.lineSeparator()
                        + System.lineSeparator()
                        + "Availability" + System.lineSeparator()
                        + blankFallback(applicant.availability())
        );
    }

    private String blankFallback(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void handleLogout() {
        if (!sessionId.isBlank()) {
            loginService.expireSession(sessionId);
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

    private JTextArea buildReadonlyTextArea() {
        JTextArea area = new JTextArea();
        UiTheme.styleTextArea(area);
        area.setEditable(false);
        return area;
    }
}
