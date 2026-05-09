package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.applications.CsvJobApplicationRepository;
import com.group30.tarecruitment.applications.JobApplicationService;
import com.group30.tarecruitment.applications.TaApplicationSummary;
import com.group30.tarecruitment.jobs.CsvJobPostingRepository;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.jobs.JobPostingService;
import com.group30.tarecruitment.login.TaLoginService;
import com.group30.tarecruitment.matching.SkillMatchResult;
import com.group30.tarecruitment.matching.SkillMatchingService;
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
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Path;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class TaDashboardFrame extends JFrame {

    private static final String CARD_HOME = "HOME";
    private static final String CARD_PROFILE = "PROFILE";
    private static final String CARD_JOBS = "JOBS";
    private static final String CARD_APPLICATIONS = "APPLICATIONS";
    private static final String FILTER_ALL = "All";

    private final String email;
    private final String sessionId;
    private final TaLoginService loginService;
    private final TaProfileService profileService;
    private final JobPostingService jobPostingService;
    private final JobApplicationService applicationService;
    private final SkillMatchingService skillMatchingService;
    private final Runnable showLoginFrame;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);

    private final JLabel profileStatusValue = new JLabel();
    private final JLabel cvStatusValue = new JLabel();
    private final JLabel openJobsValue = new JLabel();
    private final JLabel applicationCountValue = new JLabel();

    private final JTextField fullNameField = new JTextField();
    private final JTextField studentIdField = new JTextField();
    private final JTextField contactEmailField = new JTextField();
    private final JTextField degreeField = new JTextField();
    private final JTextField gpaField = new JTextField();
    private final JTextArea skillsArea = new JTextArea(4, 20);
    private final JTextArea availabilityArea = new JTextArea(4, 20);
    private final JLabel currentCvLabel = new JLabel("No CV uploaded");

    private final JTextField jobSearchField = new JTextField();
    private final JComboBox<String> moduleFilterBox = new JComboBox<>();
    private final JComboBox<String> skillFilterBox = new JComboBox<>();
    private final JButton clearFiltersButton = UiTheme.secondaryButton("Clear Filters");
    private final DefaultListModel<JobPosting> jobListModel = new DefaultListModel<>();
    private final JList<JobPosting> jobList = new JList<>(jobListModel);
    private final JLabel jobResultHintLabel = new JLabel("Browse open jobs.");
    private final JLabel jobTitleLabel = new JLabel("Select a job");
    private final JLabel jobMetaLabel = new JLabel("No job selected.");
    private final JTextArea jobDescriptionArea = buildReadonlyTextArea();
    private final JTextArea jobSkillArea = buildReadonlyTextArea();
    private final JLabel jobMatchScoreLabel = UiTheme.tagLabel("0% match", UiTheme.WARNING, UiTheme.WARNING_SOFT);
    private final JPanel matchedSkillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JPanel missingSkillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
    private final JTextArea jobRecommendationArea = buildReadonlyTextArea();
    private final JButton applyButton = UiTheme.primaryButton("Apply Now");

    private final DefaultTableModel applicationTableModel = new DefaultTableModel(
            new Object[]{"Job Title", "Module", "Applied", "Status"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable applicationTable = new JTable(applicationTableModel);
    private final JLabel applicationTitleLabel = new JLabel("Select an application");
    private final JLabel applicationMetaLabel = new JLabel("No application selected.");
    private final JTextArea applicationNotesArea = buildReadonlyTextArea();
    private final JButton withdrawButton = UiTheme.secondaryButton("Withdraw Application");
    private final List<TaApplicationSummary> currentApplications = new ArrayList<>();

    private boolean returningToLogin;
    private boolean updatingJobFilters;
    private String selectedCvPath = "";

    public TaDashboardFrame(
            String email,
            String sessionId,
            TaLoginService loginService,
            TaProfileService profileService,
            JobPostingService jobPostingService,
            JobApplicationService applicationService,
            SkillMatchingService skillMatchingService,
            Runnable showLoginFrame
    ) {
        this.email = email == null ? "" : email.trim().toLowerCase();
        this.sessionId = sessionId;
        this.loginService = loginService;
        this.profileService = profileService;
        this.jobPostingService = jobPostingService;
        this.applicationService = applicationService;
        this.skillMatchingService = skillMatchingService;
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

        styleFields();
        installInteractions();

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.APP_BACKGROUND);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMainArea(), BorderLayout.CENTER);
        setContentPane(root);

        loadProfile();
        refreshJobFilters();
        refreshJobList();
        refreshApplications();
        refreshDashboardSummary();
    }

    public TaDashboardFrame(String sessionId, TaLoginService loginService, Runnable showLoginFrame) {
        this(
                "",
                sessionId,
                loginService,
                new TaProfileService(new CsvTaProfileRepository(Path.of("data", "ta_profile.csv"))),
                new JobPostingService(new CsvJobPostingRepository(Path.of("data", "job_posting.csv")), Clock.systemDefaultZone()),
                new JobApplicationService(
                        new CsvJobApplicationRepository(Path.of("data", "job_application.csv")),
                        new CsvJobPostingRepository(Path.of("data", "job_posting.csv")),
                        new CsvTaProfileRepository(Path.of("data", "ta_profile.csv")),
                        Clock.systemDefaultZone()
                ),
                new SkillMatchingService(),
                showLoginFrame
        );
    }

    private void styleFields() {
        UiTheme.styleTextArea(skillsArea);
        UiTheme.styleTextArea(availabilityArea);
        UiTheme.styleTextArea(applicationNotesArea);
        UiTheme.styleTextArea(jobRecommendationArea);
        applicationNotesArea.setEditable(false);
        jobRecommendationArea.setEditable(false);
        currentCvLabel.setFont(UiTheme.BODY_FONT);
        matchedSkillsPanel.setOpaque(false);
        missingSkillsPanel.setOpaque(false);
        jobRecommendationArea.setRows(4);

        UiTheme.styleInput(fullNameField);
        UiTheme.styleInput(studentIdField);
        UiTheme.styleInput(contactEmailField);
        UiTheme.styleInput(degreeField);
        UiTheme.styleInput(gpaField);
        UiTheme.styleInput(jobSearchField);
        UiTheme.styleInput(moduleFilterBox);
        UiTheme.styleInput(skillFilterBox);
    }

    private void installInteractions() {
        jobList.setCellRenderer(new JobRenderer());
        jobList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedJob(jobList.getSelectedValue());
            }
        });

        applicationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        applicationTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = applicationTable.getSelectedRow();
                showSelectedApplication(selectedRow >= 0 && selectedRow < currentApplications.size()
                        ? currentApplications.get(selectedRow)
                        : null);
            }
        });

        bindLiveRefresh(jobSearchField, this::refreshJobList);
        moduleFilterBox.addActionListener(e -> {
            if (!updatingJobFilters) {
                refreshJobList();
            }
        });
        skillFilterBox.addActionListener(e -> {
            if (!updatingJobFilters) {
                refreshJobList();
            }
        });
        clearFiltersButton.addActionListener(e -> {
            updatingJobFilters = true;
            jobSearchField.setText("");
            moduleFilterBox.setSelectedItem(FILTER_ALL);
            skillFilterBox.setSelectedItem(FILTER_ALL);
            updatingJobFilters = false;
            refreshJobList();
        });
        applyButton.addActionListener(e -> applyForSelectedJob());
        withdrawButton.addActionListener(e -> withdrawSelectedApplication());
        withdrawButton.setEnabled(false);
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
        homeButton.addActionListener(e -> {
            refreshDashboardSummary();
            cardLayout.show(contentPanel, CARD_HOME);
        });
        sidebar.add(homeButton);

        JButton profileButton = UiTheme.navButton("Profile");
        profileButton.addActionListener(e -> {
            loadProfile();
            cardLayout.show(contentPanel, CARD_PROFILE);
        });
        sidebar.add(profileButton);

        JButton jobsButton = UiTheme.navButton("Find Jobs");
        jobsButton.addActionListener(e -> {
            refreshJobFilters();
            refreshJobList();
            cardLayout.show(contentPanel, CARD_JOBS);
        });
        sidebar.add(jobsButton);

        JButton applicationsButton = UiTheme.navButton("My Applications");
        applicationsButton.addActionListener(e -> {
            refreshApplications();
            cardLayout.show(contentPanel, CARD_APPLICATIONS);
        });
        sidebar.add(applicationsButton);

        JButton messagesButton = UiTheme.navButton("Messages");
        messagesButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Messaging is planned for a later sprint."));
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
        contentPanel.add(createApplicationsCard(), CARD_APPLICATIONS);
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setBackground(UiTheme.APP_BACKGROUND);

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filters.setOpaque(false);

        jobSearchField.setPreferredSize(new Dimension(280, UiTheme.FIELD_HEIGHT));
        moduleFilterBox.setPreferredSize(new Dimension(160, UiTheme.FIELD_HEIGHT));
        skillFilterBox.setPreferredSize(new Dimension(170, UiTheme.FIELD_HEIGHT));

        filters.add(jobSearchField);
        filters.add(moduleFilterBox);
        filters.add(skillFilterBox);
        filters.add(clearFiltersButton);
        topBar.add(filters, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        right.add(new JLabel("TA User"));
        right.add(new JLabel(email.isBlank() ? "ta@g30.local" : email));
        topBar.add(right, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createHomeCard() {
        JPanel card = new JPanel(new GridLayout(2, 2, 18, 18));
        card.setOpaque(false);
        card.add(buildMetricCard("Profile Status", profileStatusValue));
        card.add(buildMetricCard("CV Status", cvStatusValue));
        card.add(buildMetricCard("Open Jobs", openJobsValue));
        card.add(buildMetricCard("Applications", applicationCountValue));
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
        details.add(UiTheme.verticalGap(14));
        details.add(createCvCard());

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

    private JPanel createCvCard() {
        JPanel cvCard = new JPanel(new BorderLayout(12, 12));
        cvCard.setBackground(UiTheme.PANEL_BACKGROUND);
        cvCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JLabel title = new JLabel("CV Upload");
        title.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        cvCard.add(title, BorderLayout.NORTH);

        cvCard.add(currentCvLabel, BorderLayout.CENTER);

        JButton uploadButton = UiTheme.secondaryButton("Upload / Replace CV");
        uploadButton.addActionListener(e -> chooseAndUploadCv());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(uploadButton);
        cvCard.add(footer, BorderLayout.SOUTH);
        return cvCard;
    }

    private JPanel createJobsCard() {
        JPanel left = new JPanel(new BorderLayout(12, 12));
        left.setBackground(UiTheme.PANEL_BACKGROUND);
        left.setBorder(UiTheme.cardBorder());

        JPanel leftHeader = new JPanel(new BorderLayout(8, 8));
        leftHeader.setOpaque(false);
        JLabel leftTitle = new JLabel("Available TA Jobs");
        leftTitle.setFont(UiTheme.SECTION_FONT);
        leftHeader.add(leftTitle, BorderLayout.WEST);
        leftHeader.add(jobResultHintLabel, BorderLayout.SOUTH);
        left.add(leftHeader, BorderLayout.NORTH);
        left.add(new JScrollPane(jobList), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(12, 12));
        right.setBackground(UiTheme.PANEL_BACKGROUND);
        right.setBorder(UiTheme.cardBorder());

        JPanel header = new JPanel(new BorderLayout(12, 12));
        header.setOpaque(false);
        jobTitleLabel.setFont(UiTheme.SECTION_FONT);
        header.add(jobTitleLabel, BorderLayout.WEST);
        header.add(applyButton, BorderLayout.EAST);
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
        JLabel skillTitle = new JLabel("Required Skills / Notes");
        skillTitle.setFont(UiTheme.SECTION_FONT);
        skillCard.add(skillTitle, BorderLayout.NORTH);
        JPanel analysisWrapper = new JPanel(new BorderLayout(8, 8));
        analysisWrapper.setOpaque(false);
        analysisWrapper.add(new JScrollPane(jobSkillArea), BorderLayout.NORTH);
        analysisWrapper.add(createSkillGapCard(), BorderLayout.CENTER);
        skillCard.add(analysisWrapper, BorderLayout.CENTER);

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

    private JPanel createSkillGapCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(UiTheme.SIDEBAR_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        JPanel header = new JPanel(new BorderLayout(10, 10));
        header.setOpaque(false);
        JLabel title = new JLabel("AI Skill Gap Analysis");
        title.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        header.add(title, BorderLayout.WEST);
        header.add(jobMatchScoreLabel, BorderLayout.EAST);
        card.add(header, BorderLayout.NORTH);

        JPanel body = new JPanel();
        body.setOpaque(false);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.add(buildSkillGroup("Matched Skills", matchedSkillsPanel));
        body.add(UiTheme.verticalGap(10));
        body.add(buildSkillGroup("Missing Skills", missingSkillsPanel));
        body.add(UiTheme.verticalGap(10));
        body.add(new JScrollPane(jobRecommendationArea));
        card.add(body, BorderLayout.CENTER);
        return card;
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

    private JPanel createApplicationsCard() {
        JPanel left = new JPanel(new BorderLayout(12, 12));
        left.setBackground(UiTheme.PANEL_BACKGROUND);
        left.setBorder(UiTheme.cardBorder());
        JLabel leftTitle = new JLabel("My Applications");
        leftTitle.setFont(UiTheme.SECTION_FONT);
        left.add(leftTitle, BorderLayout.NORTH);
        left.add(new JScrollPane(applicationTable), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(12, 12));
        right.setBackground(UiTheme.PANEL_BACKGROUND);
        right.setBorder(UiTheme.cardBorder());
        applicationTitleLabel.setFont(UiTheme.SECTION_FONT);
        right.add(applicationTitleLabel, BorderLayout.NORTH);

        JPanel details = new JPanel(new BorderLayout(8, 8));
        details.setOpaque(false);
        details.add(applicationMetaLabel, BorderLayout.NORTH);
        details.add(new JScrollPane(applicationNotesArea), BorderLayout.CENTER);
        right.add(details, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(withdrawButton);
        right.add(actions, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setResizeWeight(0.54);
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
        selectedCvPath = profile.cvFilePath();
        updateCvLabel();
        updateSelectedJobAnalysis();
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
                    selectedCvPath
            ));
            updateSelectedJobAnalysis();
            refreshDashboardSummary();
            JOptionPane.showMessageDialog(this, "Profile saved.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Profile save failed: " + ex.getMessage());
        }
    }

    private void chooseAndUploadCv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select CV File");
        chooser.setFileFilter(new FileNameExtensionFilter("CV Files (.pdf, .txt)", "pdf", "txt"));
        int choice = chooser.showOpenDialog(this);
        if (choice != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = chooser.getSelectedFile();
        try {
            TaProfile updated = profileService.attachCv(email, selectedFile.getAbsolutePath());
            selectedCvPath = updated.cvFilePath();
            updateCvLabel();
            refreshDashboardSummary();
            JOptionPane.showMessageDialog(this, "CV uploaded successfully.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "CV upload failed: " + ex.getMessage());
        }
    }

    private void refreshJobFilters() {
        updatingJobFilters = true;
        String selectedModule = selectedFilterValue(moduleFilterBox);
        String selectedSkill = selectedFilterValue(skillFilterBox);

        populateFilterBox(moduleFilterBox, jobPostingService.listOpenJobModules(), selectedModule);
        populateFilterBox(skillFilterBox, jobPostingService.listOpenJobSkills(), selectedSkill);
        updatingJobFilters = false;
    }

    private void populateFilterBox(JComboBox<String> comboBox, List<String> values, String selection) {
        comboBox.removeAllItems();
        comboBox.addItem(FILTER_ALL);
        for (String value : values) {
            comboBox.addItem(value);
        }
        if (selection != null && !selection.isBlank()) {
            comboBox.setSelectedItem(selection);
        } else {
            comboBox.setSelectedItem(FILTER_ALL);
        }
    }

    private void refreshJobList() {
        String previousSelection = jobList.getSelectedValue() == null ? "" : jobList.getSelectedValue().jobId();
        jobListModel.clear();

        List<JobPosting> results = jobPostingService.browseOpenJobs(
                jobSearchField.getText(),
                selectedFilterValue(skillFilterBox),
                selectedFilterValue(moduleFilterBox)
        );
        for (JobPosting posting : results) {
            jobListModel.addElement(posting);
        }

        if (!results.isEmpty()) {
            int selectedIndex = findJobIndex(previousSelection);
            jobList.setSelectedIndex(selectedIndex >= 0 ? selectedIndex : 0);
            jobResultHintLabel.setText(results.size() + " open job(s) found.");
        } else {
            showSelectedJob(null);
            jobResultHintLabel.setText(hasActiveFilters()
                    ? "No jobs match the current search and filters."
                    : "No open jobs are available right now.");
        }
        refreshDashboardSummary();
    }

    private int findJobIndex(String jobId) {
        for (int i = 0; i < jobListModel.size(); i++) {
            if (jobListModel.get(i).jobId().equals(jobId)) {
                return i;
            }
        }
        return -1;
    }

    private boolean hasActiveFilters() {
        return !jobSearchField.getText().trim().isBlank()
                || !selectedFilterValue(moduleFilterBox).isBlank()
                || !selectedFilterValue(skillFilterBox).isBlank();
    }

    private String selectedFilterValue(JComboBox<String> comboBox) {
        Object selected = comboBox.getSelectedItem();
        if (selected == null) {
            return "";
        }
        String value = selected.toString().trim();
        return FILTER_ALL.equalsIgnoreCase(value) ? "" : value;
    }

    private void showSelectedJob(JobPosting job) {
        if (job == null) {
            jobTitleLabel.setText("Select a job");
            jobMetaLabel.setText("No open jobs available.");
            jobDescriptionArea.setText("");
            jobSkillArea.setText("");
            updateSkillGapUi(new SkillMatchResult(0, List.of(), List.of(), "Select a job to compare its required skills with your saved profile."), true);
            applyButton.setEnabled(false);
            return;
        }
        jobTitleLabel.setText(job.moduleCode() + ": " + job.title());
        jobMetaLabel.setText(buildJobMeta(job));
        jobDescriptionArea.setText(buildJobDescription(job));
        jobSkillArea.setText(buildSkillNotes(job));
        updateSkillGapUi(skillMatchingService.analyze(profileService.loadProfile(email), job), false);
        applyButton.setEnabled(true);
    }

    private String buildJobMeta(JobPosting job) {
        String typeLabel = job.isInvigilation() ? "Invigilation" : "TA";
        return typeLabel + " | " + job.hoursPerWeek() + " hrs/week | Deadline " + job.applicationDeadline();
    }

    private String buildJobDescription(JobPosting job) {
        StringBuilder builder = new StringBuilder();
        builder.append(job.description());
        builder.append(System.lineSeparator()).append(System.lineSeparator());
        if (job.isInvigilation()) {
            builder.append("Exam Date: ").append(blankFallback(job.examDate() == null ? "" : job.examDate().toString())).append(System.lineSeparator());
            builder.append("Exam Time: ").append(blankFallback(job.examTime())).append(System.lineSeparator());
            builder.append("Location: ").append(blankFallback(job.location())).append(System.lineSeparator());
            builder.append("Invigilators Needed: ").append(job.invigilatorsNeeded());
        } else {
            builder.append("Module Code: ").append(job.moduleCode());
        }
        return builder.toString();
    }

    private String buildSkillNotes(JobPosting job) {
        String skills = blankFallback(job.requiredSkills());
        if (job.isInvigilation()) {
            return "Required Skills: " + skills + System.lineSeparator() + System.lineSeparator()
                    + "This is an invigilation opportunity. Application review follows the same workflow as standard TA jobs.";
        }
        return skills;
    }

    private void applyForSelectedJob() {
        JobPosting selectedJob = jobList.getSelectedValue();
        if (selectedJob == null) {
            JOptionPane.showMessageDialog(this, "Please select a job first.");
            return;
        }
        try {
            applicationService.submitApplication(email, selectedJob.jobId());
            refreshApplications();
            refreshDashboardSummary();
            cardLayout.show(contentPanel, CARD_APPLICATIONS);
            JOptionPane.showMessageDialog(this, "Application submitted successfully.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Application failed: " + ex.getMessage());
        }
    }

    private void refreshApplications() {
        String selectedApplicationId = selectedApplicationId();
        currentApplications.clear();
        currentApplications.addAll(applicationService.listApplicationsForTa(email));
        applicationTableModel.setRowCount(0);
        for (TaApplicationSummary application : currentApplications) {
            applicationTableModel.addRow(new Object[]{
                    application.jobTitle(),
                    application.moduleCode(),
                    formatTimestamp(application.appliedAt()),
                    application.status()
            });
        }
        if (!currentApplications.isEmpty()) {
            int selectedRow = findApplicationIndex(selectedApplicationId);
            applicationTable.setRowSelectionInterval(selectedRow >= 0 ? selectedRow : 0, selectedRow >= 0 ? selectedRow : 0);
        } else {
            showSelectedApplication(null);
        }
    }

    private String selectedApplicationId() {
        int selectedRow = applicationTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentApplications.size()) {
            return "";
        }
        return currentApplications.get(selectedRow).applicationId();
    }

    private int findApplicationIndex(String applicationId) {
        for (int i = 0; i < currentApplications.size(); i++) {
            if (currentApplications.get(i).applicationId().equals(applicationId)) {
                return i;
            }
        }
        return -1;
    }

    private void showSelectedApplication(TaApplicationSummary application) {
        if (application == null) {
            applicationTitleLabel.setText("Select an application");
            applicationMetaLabel.setText("No applications submitted yet.");
            applicationNotesArea.setText("Apply for an open job to see its latest review status here.");
            withdrawButton.setEnabled(false);
            return;
        }

        applicationTitleLabel.setText(application.moduleCode() + ": " + application.jobTitle());
        applicationMetaLabel.setText("Applied " + formatTimestamp(application.appliedAt()) + " | Status " + application.status());
        applicationNotesArea.setText(switch (application.status()) {
            case "ACCEPTED" -> "Your application has been accepted by the module organiser. The assigned weekly hours will now appear in the admin workload view.";
            case "REJECTED" -> "This application was not selected. You can continue applying to other open TA roles.";
            case "WITHDRAWN" -> "You withdrew this application before a decision was made.";
            default -> "This application is still pending review. You can withdraw it before the module organiser records a decision.";
        });
        withdrawButton.setEnabled("PENDING".equalsIgnoreCase(application.status()));
    }

    private void withdrawSelectedApplication() {
        int selectedRow = applicationTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= currentApplications.size()) {
            JOptionPane.showMessageDialog(this, "Please select a pending application first.");
            return;
        }
        TaApplicationSummary application = currentApplications.get(selectedRow);
        if (!"PENDING".equalsIgnoreCase(application.status())) {
            JOptionPane.showMessageDialog(this, "Only pending applications can be withdrawn.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Withdraw this application?",
                "Confirm Withdrawal",
                JOptionPane.YES_NO_OPTION
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            applicationService.withdrawApplication(email, application.applicationId());
            refreshApplications();
            refreshDashboardSummary();
            JOptionPane.showMessageDialog(this, "Application withdrawn.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Withdrawal failed: " + ex.getMessage());
        }
    }

    private void refreshDashboardSummary() {
        TaProfile profile = profileService.loadProfile(email);
        profileStatusValue.setText(isProfileReady(profile) ? "Ready" : "Incomplete");
        cvStatusValue.setText(profile.cvFilePath().isBlank() ? "Not Uploaded" : "Uploaded");
        openJobsValue.setText(Integer.toString(jobPostingService.browseOpenJobs().size()));
        applicationCountValue.setText(Integer.toString(applicationService.listApplicationsForTa(email).size()));
    }

    private void updateSelectedJobAnalysis() {
        showSelectedJob(jobList.getSelectedValue());
    }

    private void updateSkillGapUi(SkillMatchResult analysis, boolean emptyState) {
        if (emptyState) {
            jobMatchScoreLabel.setText("No analysis");
            jobMatchScoreLabel.setForeground(UiTheme.WARNING);
            jobMatchScoreLabel.setBackground(UiTheme.WARNING_SOFT);
            jobRecommendationArea.setText(analysis.recommendation());
            refreshTagPanel(matchedSkillsPanel, List.of(), UiTheme.SUCCESS, UiTheme.SUCCESS_SOFT);
            refreshTagPanel(missingSkillsPanel, List.of(), UiTheme.WARNING, UiTheme.WARNING_SOFT);
            return;
        }

        jobMatchScoreLabel.setText(analysis.matchScore() + "% match");
        applyScoreStyle(jobMatchScoreLabel, analysis.matchScore());
        refreshTagPanel(matchedSkillsPanel, analysis.matchedSkills(), UiTheme.SUCCESS, UiTheme.SUCCESS_SOFT);
        refreshTagPanel(missingSkillsPanel, analysis.missingSkills(), UiTheme.WARNING, UiTheme.WARNING_SOFT);
        jobRecommendationArea.setText(analysis.recommendation());
    }

    private void applyScoreStyle(JLabel label, int score) {
        Color foreground;
        Color background;
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

    private void refreshTagPanel(JPanel panel, List<String> items, Color foreground, Color background) {
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

    private boolean isProfileReady(TaProfile profile) {
        return !profile.fullName().isBlank()
                && !profile.studentId().isBlank()
                && !profile.contactEmail().isBlank();
    }

    private void updateCvLabel() {
        currentCvLabel.setText(selectedCvPath.isBlank() ? "No CV uploaded" : extractFileName(selectedCvPath));
    }

    private String extractFileName(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String normalized = value.replace('\\', '/');
        int slashIndex = normalized.lastIndexOf('/');
        return slashIndex >= 0 ? normalized.substring(slashIndex + 1) : normalized;
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

    static String jobListItemText(JobPosting job) {
        String typeLabel = job.isInvigilation() ? "Invigilation" : "TA";
        StringBuilder builder = new StringBuilder("<html><b>")
                .append(job.title())
                .append("</b><br/>")
                .append(job.moduleCode())
                .append(" | ")
                .append(typeLabel);
        if (!job.requiredSkills().isBlank()) {
            builder.append(" | Skills ").append(job.requiredSkills());
        }
        builder.append("<br/>")
                .append(job.hoursPerWeek())
                .append(" hrs/week | Deadline ")
                .append(job.applicationDeadline());
        if (job.isInvigilation() && job.examDate() != null) {
            builder.append("<br/>Exam ").append(job.examDate()).append(" @ ").append(blankStatic(job.location()));
        }
        builder.append("</html>");
        return builder.toString();
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

    private JTextArea buildReadonlyTextArea() {
        JTextArea area = new JTextArea();
        UiTheme.styleTextArea(area);
        area.setEditable(false);
        return area;
    }

    private void bindLiveRefresh(JTextField field, Runnable action) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                action.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                action.run();
            }
        });
    }

    private String blankFallback(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private static String blankStatic(String value) {
        return value == null || value.isBlank() ? "-" : value;
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
                label.setText(jobListItemText(job));
                label.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            }
            return label;
        }
    }
}
