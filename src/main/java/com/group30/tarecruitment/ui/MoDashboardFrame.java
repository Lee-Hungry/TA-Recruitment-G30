package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.applications.CsvJobApplicationRepository;
import com.group30.tarecruitment.applications.JobApplicationService;
import com.group30.tarecruitment.applications.MoApplicantView;
import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.jobs.JobPostingDraft;
import com.group30.tarecruitment.jobs.JobPostingService;
import com.group30.tarecruitment.matching.SkillMatchResult;
import com.group30.tarecruitment.matching.SkillMatchingService;
import com.group30.tarecruitment.mo.CsvMoAccountRepository;
import com.group30.tarecruitment.mo.CsvSessionRepository;
import com.group30.tarecruitment.mo.MoLoginService;
import com.group30.tarecruitment.notifications.CsvNotificationRepository;
import com.group30.tarecruitment.notifications.NotificationRecord;
import com.group30.tarecruitment.notifications.NotificationService;
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
import javax.swing.table.TableRowSorter;
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
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

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
    private final SkillMatchingService skillMatchingService;
    private final NotificationService notificationService;
    private final Runnable showLoginFrame;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final DefaultTableModel postingsModel = new DefaultTableModel(
            new Object[]{"Job Title", "Type", "Module", "Deadline", "Status"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable postingsTable = new JTable(postingsModel);
    private final DefaultTableModel applicantsModel = new DefaultTableModel(
            new Object[]{"Applicant", "Student ID", "AI Match Score", "Applied", "Status"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable applicantsTable = new JTable(applicantsModel);
    private final TableRowSorter<DefaultTableModel> applicantsSorter = new TableRowSorter<>(applicantsModel);

    private final JLabel totalPostingsValue = new JLabel();
    private final JLabel openPostingsValue = new JLabel();
    private final JLabel pendingApplicantsValue = new JLabel();

    private final JLabel postingFormTitle = new JLabel("Post a new job");
    private final JComboBox<String> jobTypeBox = new JComboBox<>(new String[]{"TA", "INVIGILATION"});
    private final JTextField jobTitleField = new JTextField();
    private final JTextField moduleCodeField = new JTextField();
    private final JTextField hoursField = new JTextField();
    private final JTextField deadlineField = new JTextField();
    private final JTextArea descriptionArea = new JTextArea(6, 20);
    private final JTextArea skillsArea = new JTextArea(4, 20);
    private final JTextField examDateField = new JTextField();
    private final JTextField examTimeField = new JTextField();
    private final JTextField locationField = new JTextField();
    private final JTextField invigilatorsField = new JTextField();
    private final JButton submitPostingButton = UiTheme.primaryButton("Publish Job");
    private final JButton cancelEditButton = UiTheme.secondaryButton("Cancel Edit");
    private final JPanel invigilationFieldsPanel = UiTheme.transparentPanel();

    private final JComboBox<JobPosting> applicantJobSelector = new JComboBox<>();
    private final JLabel applicantNameLabel = new JLabel("Select an applicant");
    private final JLabel applicantMetaLabel = new JLabel("No applicant selected.");
    private final JLabel applicantCvLabel = new JLabel("CV path: -");
    private final JLabel applicantMatchScoreLabel = UiTheme.tagLabel("0% match", UiTheme.WARNING, UiTheme.WARNING_SOFT);
    private final JTextArea applicantProfileArea = buildReadonlyTextArea();
    private final JTextArea applicantRecommendationArea = buildReadonlyTextArea();
    private final JPanel matchedSkillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JPanel missingSkillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JButton notificationsButton = UiTheme.subtleButton("Notifications");
    private final JButton acceptButton = UiTheme.primaryButton("Accept");
    private final JButton rejectButton = UiTheme.secondaryButton("Reject");

    private final List<JobPosting> currentPostings = new ArrayList<>();
    private final List<MoApplicantView> currentApplicants = new ArrayList<>();
    private final Map<String, SkillMatchResult> currentApplicantMatches = new LinkedHashMap<>();
    private boolean returningToLogin;
    private String editingJobId = "";

    public MoDashboardFrame(
            String moEmail,
            String sessionId,
            MoLoginService loginService,
            JobPostingService jobPostingService,
            JobApplicationService applicationService,
            SkillMatchingService skillMatchingService,
            NotificationService notificationService,
            Runnable showLoginFrame
    ) {
        this.moEmail = moEmail == null ? "" : moEmail.trim().toLowerCase();
        this.sessionId = sessionId;
        this.loginService = loginService;
        this.jobPostingService = jobPostingService;
        this.applicationService = applicationService;
        this.skillMatchingService = skillMatchingService;
        this.notificationService = notificationService;
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

        styleFields();
        installInteractions();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.APP_BACKGROUND);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMainArea(), BorderLayout.CENTER);
        setContentPane(root);

        refreshMyPostings();
        refreshApplicantJobOptions();
        refreshNotificationButton();
        updatePostingModeUi();
    }

    public MoDashboardFrame() {
        this(
                "",
                "",
                new MoLoginService(
                        new CsvMoAccountRepository(Path.of("data", "user_account.csv")),
                        new CsvSessionRepository(Path.of("data", "mo_session.csv"))
                ),
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
        );
    }

    private void styleFields() {
        UiTheme.styleTextArea(descriptionArea);
        UiTheme.styleTextArea(skillsArea);
        UiTheme.styleTextArea(applicantRecommendationArea);
        UiTheme.styleInput(jobTypeBox);
        UiTheme.styleInput(jobTitleField);
        UiTheme.styleInput(moduleCodeField);
        UiTheme.styleInput(hoursField);
        UiTheme.styleInput(deadlineField);
        UiTheme.styleInput(examDateField);
        UiTheme.styleInput(examTimeField);
        UiTheme.styleInput(locationField);
        UiTheme.styleInput(invigilatorsField);
        UiTheme.styleInput(applicantJobSelector);
        applicantRecommendationArea.setEditable(false);
        applicantRecommendationArea.setRows(4);
        matchedSkillsPanel.setOpaque(false);
        missingSkillsPanel.setOpaque(false);
        applicantsSorter.setComparator(2, Comparator.comparingInt(this::parsePercentValue));
        applicantsTable.setRowSorter(applicantsSorter);
    }

    private void installInteractions() {
        postingsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        postingsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                refreshPostingActionState();
            }
        });

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
                int modelRow = row < 0 ? -1 : applicantsTable.convertRowIndexToModel(row);
                showSelectedApplicant(modelRow >= 0 && modelRow < currentApplicants.size() ? currentApplicants.get(modelRow) : null);
            }
        });

        acceptButton.addActionListener(e -> reviewSelectedApplicant("ACCEPTED"));
        rejectButton.addActionListener(e -> reviewSelectedApplicant("REJECTED"));
        submitPostingButton.addActionListener(e -> savePosting());
        cancelEditButton.addActionListener(e -> clearPostingForm());
        jobTypeBox.addActionListener(e -> updatePostingModeUi());
        notificationsButton.addActionListener(e -> showNotificationsDialog());
        setApplicantActionsEnabled(false);
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
        postButton.addActionListener(e -> {
            clearPostingForm();
            cardLayout.show(contentPanel, CARD_POST);
        });
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

        JTextField searchField = new JTextField();
        UiTheme.styleInput(searchField);
        searchField.setText("Search postings, applicants...");
        topBar.add(searchField, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        right.add(notificationsButton);
        right.add(new JLabel("MO User"));
        right.add(new JLabel(moEmail.isBlank() ? "mo@g30.local" : moEmail));
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

        postingFormTitle.setFont(UiTheme.TITLE_FONT);
        card.add(postingFormTitle, BorderLayout.NORTH);

        JPanel basicsPanel = UiTheme.transparentPanel();
        basicsPanel.setLayout(new BoxLayout(basicsPanel, BoxLayout.Y_AXIS));
        basicsPanel.add(createFieldBlock("Job Type", jobTypeBox));
        basicsPanel.add(UiTheme.verticalGap(14));
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
        descriptionPanel.add(UiTheme.verticalGap(14));
        descriptionPanel.add(createInvigilationPanel());

        JPanel wrapper = new JPanel(new GridLayout(1, 2, 18, 18));
        wrapper.setOpaque(false);
        wrapper.add(basicsPanel);
        wrapper.add(descriptionPanel);
        card.add(wrapper, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);
        footer.add(cancelEditButton);
        footer.add(submitPostingButton);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createInvigilationPanel() {
        invigilationFieldsPanel.setLayout(new BoxLayout(invigilationFieldsPanel, BoxLayout.Y_AXIS));
        invigilationFieldsPanel.add(createFieldBlock("Exam Date (yyyy-MM-dd)", examDateField));
        invigilationFieldsPanel.add(UiTheme.verticalGap(14));
        invigilationFieldsPanel.add(createFieldBlock("Exam Time", examTimeField));
        invigilationFieldsPanel.add(UiTheme.verticalGap(14));
        invigilationFieldsPanel.add(createFieldBlock("Location", locationField));
        invigilationFieldsPanel.add(UiTheme.verticalGap(14));
        invigilationFieldsPanel.add(createFieldBlock("Invigilators Needed", invigilatorsField));
        return invigilationFieldsPanel;
    }

    private JPanel createMyPostingsCard() {
        JPanel card = new JPanel(new BorderLayout(18, 18));
        card.setBackground(UiTheme.PANEL_BACKGROUND);
        card.setBorder(UiTheme.cardBorder());
        JLabel title = new JLabel("My Postings");
        title.setFont(UiTheme.TITLE_FONT);
        card.add(title, BorderLayout.NORTH);
        card.add(new JScrollPane(postingsTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        JButton editButton = UiTheme.secondaryButton("Edit Selected");
        JButton deleteButton = UiTheme.secondaryButton("Delete Selected");
        JButton refreshButton = UiTheme.primaryButton("Refresh");
        editButton.addActionListener(e -> editSelectedPosting());
        deleteButton.addActionListener(e -> deleteSelectedPosting());
        refreshButton.addActionListener(e -> refreshMyPostings());
        actions.add(editButton);
        actions.add(deleteButton);
        actions.add(refreshButton);
        card.add(actions, BorderLayout.SOUTH);
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
        detailBody.add(createApplicantDetailBody(), BorderLayout.CENTER);
        right.add(detailBody, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);
        actions.add(rejectButton);
        actions.add(acceptButton);
        right.add(actions, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setResizeWeight(0.52);
        splitPane.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(splitPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createApplicantDetailBody() {
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setOpaque(false);
        body.add(new JScrollPane(applicantProfileArea), BorderLayout.NORTH);

        JPanel analysisCard = new JPanel(new BorderLayout(8, 8));
        analysisCard.setBackground(UiTheme.SIDEBAR_BACKGROUND);
        analysisCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JPanel header = new JPanel(new BorderLayout(8, 8));
        header.setOpaque(false);
        JLabel title = new JLabel("AI Match Review");
        title.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        header.add(title, BorderLayout.WEST);
        header.add(applicantMatchScoreLabel, BorderLayout.EAST);
        analysisCard.add(header, BorderLayout.NORTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(applicantCvLabel);
        content.add(Box.createVerticalStrut(10));
        content.add(buildSkillGroup("Matched Skills", matchedSkillsPanel));
        content.add(Box.createVerticalStrut(10));
        content.add(buildSkillGroup("Missing Skills", missingSkillsPanel));
        content.add(Box.createVerticalStrut(10));
        content.add(new JScrollPane(applicantRecommendationArea));
        analysisCard.add(content, BorderLayout.CENTER);

        body.add(analysisCard, BorderLayout.CENTER);
        return body;
    }

    private JPanel buildSkillGroup(String title, JPanel tagPanel) {
        JPanel group = UiTheme.transparentPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(title);
        label.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        label.setAlignmentX(LEFT_ALIGNMENT);
        tagPanel.setAlignmentX(LEFT_ALIGNMENT);
        group.add(label);
        group.add(Box.createVerticalStrut(4));
        group.add(tagPanel);
        return group;
    }

    private void savePosting() {
        try {
            JobPostingDraft draft = buildDraftFromForm();
            if (editingJobId.isBlank()) {
                jobPostingService.postJob(draft);
                JOptionPane.showMessageDialog(this, "Job posted successfully.");
            } else {
                jobPostingService.updatePosting(moEmail, editingJobId, draft);
                JOptionPane.showMessageDialog(this, "Job updated successfully.");
            }
            clearPostingForm();
            refreshMyPostings();
            refreshApplicantJobOptions();
            cardLayout.show(contentPanel, CARD_POSTINGS);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Posting failed: " + ex.getMessage());
        }
    }

    private JobPostingDraft buildDraftFromForm() {
        String jobType = selectedJobType();
        return new JobPostingDraft(
                moEmail,
                jobTitleField.getText(),
                moduleCodeField.getText(),
                descriptionArea.getText(),
                skillsArea.getText(),
                Integer.parseInt(hoursField.getText().trim()),
                LocalDate.parse(deadlineField.getText().trim()),
                jobType,
                "INVIGILATION".equals(jobType) ? LocalDate.parse(examDateField.getText().trim()) : null,
                "INVIGILATION".equals(jobType) ? examTimeField.getText() : "",
                "INVIGILATION".equals(jobType) ? locationField.getText() : "",
                "INVIGILATION".equals(jobType) ? Integer.parseInt(invigilatorsField.getText().trim()) : null
        );
    }

    private void clearPostingForm() {
        editingJobId = "";
        jobTypeBox.setSelectedItem("TA");
        jobTitleField.setText("");
        moduleCodeField.setText("");
        hoursField.setText("");
        deadlineField.setText("");
        descriptionArea.setText("");
        skillsArea.setText("");
        examDateField.setText("");
        examTimeField.setText("");
        locationField.setText("");
        invigilatorsField.setText("");
        updatePostingModeUi();
    }

    private void updatePostingModeUi() {
        boolean invigilation = "INVIGILATION".equals(selectedJobType());
        invigilationFieldsPanel.setVisible(invigilation);
        postingFormTitle.setText(editingJobId.isBlank() ? "Post a new job" : "Edit selected job");
        submitPostingButton.setText(editingJobId.isBlank() ? "Publish Job" : "Save Changes");
        cancelEditButton.setVisible(!editingJobId.isBlank());
        skillsArea.setEnabled(true);
        skillsArea.setToolTipText(invigilation
                ? "Optional for invigilation jobs."
                : "List required skills separated by commas.");
        revalidate();
        repaint();
    }

    private String selectedJobType() {
        Object selected = jobTypeBox.getSelectedItem();
        return selected == null ? "TA" : selected.toString().trim().toUpperCase();
    }

    private void refreshMyPostings() {
        String selectedJobId = selectedPostingId();
        postingsModel.setRowCount(0);
        currentPostings.clear();
        List<JobPosting> postings = jobPostingService.viewPostingsByOwner(moEmail);
        currentPostings.addAll(postings);
        int openCount = 0;
        for (JobPosting posting : postings) {
            postingsModel.addRow(new Object[]{
                    posting.title(),
                    posting.isInvigilation() ? "Invigilation" : "TA",
                    posting.moduleCode(),
                    posting.applicationDeadline(),
                    posting.status()
            });
            if ("OPEN".equalsIgnoreCase(posting.status())) {
                openCount++;
            }
        }
        totalPostingsValue.setText(Integer.toString(postings.size()));
        openPostingsValue.setText(Integer.toString(openCount));
        pendingApplicantsValue.setText(Integer.toString(countPendingApplicants(postings)));

        if (!currentPostings.isEmpty()) {
            int selectedIndex = findPostingIndex(selectedJobId);
            postingsTable.setRowSelectionInterval(selectedIndex >= 0 ? selectedIndex : 0, selectedIndex >= 0 ? selectedIndex : 0);
        }
        refreshPostingActionState();
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
        List<JobPosting> postings = jobPostingService.viewPostingsByOwner(moEmail);
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
        currentApplicantMatches.clear();
        if (selectedJob == null) {
            showSelectedApplicant(null);
            return;
        }

        List<MoApplicantView> applicants = new ArrayList<>(applicationService.listApplicantsForJob(moEmail, selectedJob.jobId()));
        currentApplicantMatches.putAll(skillMatchingService.analyzeApplicants(selectedJob, applicants));
        applicants.sort((left, right) -> Integer.compare(matchScoreFor(right, selectedJob), matchScoreFor(left, selectedJob)));
        currentApplicants.addAll(applicants);
        for (MoApplicantView applicant : currentApplicants) {
            SkillMatchResult matchResult = currentApplicantMatches.getOrDefault(
                    applicant.applicationId(),
                    skillMatchingService.analyze(applicant.skills(), selectedJob.requiredSkills())
            );
            applicantsModel.addRow(new Object[]{
                    applicant.fullName(),
                    applicant.studentId(),
                    matchResult.matchScore() + "%",
                    formatTimestamp(applicant.appliedAt()),
                    applicant.status()
            });
        }
        if (!currentApplicants.isEmpty()) {
            applicantsSorter.setSortKeys(List.of(new javax.swing.RowSorter.SortKey(2, javax.swing.SortOrder.DESCENDING)));
            applicantsSorter.sort();
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
            applicantProfileArea.setText("Choose a posting and an applicant to review the TA profile, uploaded CV path, and latest application status.");
            updateApplicantMatchUi(new SkillMatchResult(0, List.of(), List.of(), "Select an applicant to review the match analysis."));
            setApplicantActionsEnabled(false);
            return;
        }

        applicantNameLabel.setText(applicant.fullName());
        applicantMetaLabel.setText(applicant.moduleCode() + " | Applied " + formatTimestamp(applicant.appliedAt()) + " | Status " + applicant.status());
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
        SkillMatchResult matchResult = currentApplicantMatches.getOrDefault(
                applicant.applicationId(),
                skillMatchingService.analyze(applicant.skills(), currentSelectedJobSkills())
        );
        updateApplicantMatchUi(matchResult);
        setApplicantActionsEnabled(true);
    }

    private void reviewSelectedApplicant(String nextStatus) {
        int selectedRow = applicantsTable.getSelectedRow();
        int modelRow = selectedRow < 0 ? -1 : applicantsTable.convertRowIndexToModel(selectedRow);
        if (modelRow < 0 || modelRow >= currentApplicants.size()) {
            JOptionPane.showMessageDialog(this, "Please select an applicant first.");
            return;
        }

        MoApplicantView applicant = currentApplicants.get(modelRow);
        try {
            applicationService.updateApplicationStatus(moEmail, applicant.applicationId(), nextStatus);
            refreshApplicantsForSelectedJob();
            refreshMyPostings();
            refreshNotificationButton();
            JOptionPane.showMessageDialog(this, "Application updated to " + nextStatus + ".");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Decision failed: " + ex.getMessage());
        }
    }

    private void editSelectedPosting() {
        JobPosting posting = selectedPosting();
        if (posting == null) {
            JOptionPane.showMessageDialog(this, "Please select a posting first.");
            return;
        }
        editingJobId = posting.jobId();
        jobTypeBox.setSelectedItem(posting.jobType());
        jobTitleField.setText(posting.title());
        moduleCodeField.setText(posting.moduleCode());
        hoursField.setText(Integer.toString(posting.hoursPerWeek()));
        deadlineField.setText(posting.applicationDeadline().toString());
        descriptionArea.setText(posting.description());
        skillsArea.setText(posting.requiredSkills());
        examDateField.setText(posting.examDate() == null ? "" : posting.examDate().toString());
        examTimeField.setText(posting.examTime());
        locationField.setText(posting.location());
        invigilatorsField.setText(posting.invigilatorsNeeded() <= 0 ? "" : Integer.toString(posting.invigilatorsNeeded()));
        updatePostingModeUi();
        cardLayout.show(contentPanel, CARD_POST);
    }

    private void deleteSelectedPosting() {
        JobPosting posting = selectedPosting();
        if (posting == null) {
            JOptionPane.showMessageDialog(this, "Please select a posting first.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Delete the selected posting?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            jobPostingService.deletePosting(moEmail, posting.jobId());
            if (posting.jobId().equals(editingJobId)) {
                clearPostingForm();
            }
            refreshMyPostings();
            refreshApplicantJobOptions();
            JOptionPane.showMessageDialog(this, "Posting deleted.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
        }
    }

    private JobPosting selectedPosting() {
        int row = postingsTable.getSelectedRow();
        if (row < 0 || row >= currentPostings.size()) {
            return null;
        }
        return currentPostings.get(row);
    }

    private String selectedPostingId() {
        JobPosting selected = selectedPosting();
        return selected == null ? "" : selected.jobId();
    }

    private int findPostingIndex(String jobId) {
        for (int i = 0; i < currentPostings.size(); i++) {
            if (currentPostings.get(i).jobId().equals(jobId)) {
                return i;
            }
        }
        return -1;
    }

    private void refreshPostingActionState() {
        boolean hasSelection = selectedPosting() != null;
        postingsTable.setEnabled(true);
        if (!hasSelection && !currentPostings.isEmpty()) {
            postingsTable.setRowSelectionInterval(0, 0);
        }
    }

    private void setApplicantActionsEnabled(boolean enabled) {
        acceptButton.setEnabled(enabled);
        rejectButton.setEnabled(enabled);
    }

    private String blankFallback(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private String formatTimestamp(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        try {
            return OffsetDateTime.parse(value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (DateTimeParseException ex) {
            return value;
        }
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

    private JPanel createFieldBlock(String labelText, Component field) {
        JPanel block = UiTheme.transparentPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        label.setAlignmentX(LEFT_ALIGNMENT);
        if (field instanceof javax.swing.JComponent component) {
            component.setAlignmentX(LEFT_ALIGNMENT);
        }
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

    private int matchScoreFor(MoApplicantView applicant, JobPosting selectedJob) {
        return currentApplicantMatches.getOrDefault(
                applicant.applicationId(),
                skillMatchingService.analyze(applicant.skills(), selectedJob.requiredSkills())
        ).matchScore();
    }

    private String currentSelectedJobSkills() {
        JobPosting selectedJob = (JobPosting) applicantJobSelector.getSelectedItem();
        return selectedJob == null ? "" : selectedJob.requiredSkills();
    }

    private void updateApplicantMatchUi(SkillMatchResult matchResult) {
        applicantMatchScoreLabel.setText(matchResult.matchScore() + "% match");
        applyScoreStyle(applicantMatchScoreLabel, matchResult.matchScore());
        refreshTagPanel(matchedSkillsPanel, matchResult.matchedSkills(), UiTheme.SUCCESS, UiTheme.SUCCESS_SOFT);
        refreshTagPanel(missingSkillsPanel, matchResult.missingSkills(), UiTheme.WARNING, UiTheme.WARNING_SOFT);
        applicantRecommendationArea.setText(matchResult.recommendation());
    }

    private void applyScoreStyle(JLabel label, int score) {
        java.awt.Color foreground;
        java.awt.Color background;
        if (score >= 80) {
            foreground = UiTheme.SUCCESS;
            background = UiTheme.SUCCESS_SOFT;
        } else if (score >= 50) {
            foreground = UiTheme.WARNING;
            background = UiTheme.WARNING_SOFT;
        } else {
            foreground = UiTheme.DANGER;
            background = UiTheme.DANGER_SOFT;
        }
        label.setForeground(foreground);
        label.setBackground(background);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(background.darker(), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
    }

    private void refreshTagPanel(JPanel panel, List<String> items, java.awt.Color foreground, java.awt.Color background) {
        panel.removeAll();
        if (items.isEmpty()) {
            panel.add(UiTheme.tagLabel("None", foreground, background));
        } else {
            for (String item : items) {
                panel.add(UiTheme.tagLabel(item, foreground, background));
            }
        }
        panel.revalidate();
        panel.repaint();
    }

    private void showNotificationsDialog() {
        if (notificationService == null) {
            JOptionPane.showMessageDialog(this, "Notifications are not available.");
            return;
        }
        List<NotificationRecord> notifications = notificationService.listNotificationsFor(moEmail, "MO");
        JTextArea area = buildReadonlyTextArea();
        area.setRows(12);
        if (notifications.isEmpty()) {
            area.setText("No notifications yet.");
        } else {
            StringBuilder builder = new StringBuilder();
            for (NotificationRecord notification : notifications) {
                builder.append(notification.isRead() ? "[Read] " : "[New] ")
                        .append(notification.title())
                        .append(System.lineSeparator())
                        .append(notification.message())
                        .append(System.lineSeparator())
                        .append(formatTimestamp(notification.createdAt()))
                        .append(System.lineSeparator())
                        .append(System.lineSeparator());
            }
            area.setText(builder.toString().trim());
        }
        JScrollPane pane = new JScrollPane(area);
        pane.setPreferredSize(new Dimension(520, 280));
        JOptionPane.showMessageDialog(this, pane, "Notifications", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshNotificationButton() {
        if (notificationService == null) {
            notificationsButton.setText("Notifications");
            return;
        }
        int count = notificationService.listNotificationsFor(moEmail, "MO").size();
        notificationsButton.setText("Notifications (" + count + ")");
    }

    private int parsePercentValue(Object value) {
        if (value == null) {
            return 0;
        }
        String text = value.toString().replace("%", "").trim();
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}
