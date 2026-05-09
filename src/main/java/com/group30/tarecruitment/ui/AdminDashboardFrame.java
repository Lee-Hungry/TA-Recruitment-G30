package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.admin.AdminUserAccountService;
import com.group30.tarecruitment.admin.ManagedUserAccount;
import com.group30.tarecruitment.admin.TaWorkloadService;
import com.group30.tarecruitment.admin.TaWorkloadSummary;
import com.group30.tarecruitment.admin.WorkloadSuggestion;
import com.group30.tarecruitment.admin.WorkloadSuggestionService;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.jobs.JobPostingDraft;
import com.group30.tarecruitment.jobs.JobPostingService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AdminDashboardFrame extends JFrame {

    private static final String CARD_WORKLOAD = "WORKLOAD";
    private static final String CARD_USERS = "USERS";
    private static final String CARD_POSTING = "POSTING";

    private final String adminEmail;
    private final TaWorkloadService workloadService;
    private final WorkloadSuggestionService workloadSuggestionService;
    private final AdminUserAccountService userAccountService;
    private final JobPostingService jobPostingService;
    private final Runnable showLoginFrame;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final JTextField searchField = new JTextField();

    private final DefaultTableModel workloadModel = new DefaultTableModel(
            new Object[]{"TA Name", "Student ID", "Assigned Modules", "Weekly Hours", "Overload Status"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable workloadTable = new JTable(workloadModel);
    private final JLabel thresholdValue = new JLabel();
    private final JLabel overloadedCountValue = new JLabel();
    private final JLabel taCountValue = new JLabel();
    private final JLabel selectedTaLabel = new JLabel("Select a TA");
    private final JTextArea selectedTaDetail = buildReadonlyTextArea();
    private final JTextArea workloadSuggestionArea = buildReadonlyTextArea();
    private final List<TaWorkloadSummary> currentRows = new ArrayList<>();
    private final JComboBox<WorkloadSuggestion> suggestionSelector = new JComboBox<>();
    private final JButton dismissSuggestionButton = UiTheme.secondaryButton("Dismiss");
    private final JButton focusSuggestionButton = UiTheme.primaryButton("Focus TA");
    private final List<WorkloadSuggestion> currentSuggestions = new ArrayList<>();
    private final Set<String> dismissedSuggestionKeys = new HashSet<>();

    private final DefaultTableModel accountModel = new DefaultTableModel(
            new Object[]{"Name", "Email", "Role", "Status", "Student ID"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable accountTable = new JTable(accountModel);
    private final JLabel selectedAccountLabel = new JLabel("Select an account");
    private final JTextArea selectedAccountDetail = buildReadonlyTextArea();
    private final JButton deactivateButton = UiTheme.secondaryButton("Deactivate Account");
    private final List<ManagedUserAccount> currentAccounts = new ArrayList<>();

    private final JTextField postingTitleField = new JTextField();
    private final JTextField postingModuleField = new JTextField();
    private final JTextField postingHoursField = new JTextField();
    private final JTextField postingDeadlineField = new JTextField();
    private final JTextField examDateField = new JTextField();
    private final JTextField examTimeField = new JTextField();
    private final JTextField locationField = new JTextField();
    private final JTextField invigilatorsField = new JTextField();
    private final JTextArea postingDescriptionArea = new JTextArea(5, 20);
    private final JTextArea postingSkillsArea = new JTextArea(4, 20);
    private final DefaultTableModel invigilationModel = new DefaultTableModel(
            new Object[]{"Job Title", "Module", "Exam Date", "Location", "Status"},
            0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable invigilationTable = new JTable(invigilationModel);

    private boolean returningToLogin;
    private String activeCard = CARD_WORKLOAD;

    public AdminDashboardFrame(
            String adminEmail,
            TaWorkloadService workloadService,
            WorkloadSuggestionService workloadSuggestionService,
            AdminUserAccountService userAccountService,
            JobPostingService jobPostingService,
            Runnable showLoginFrame
    ) {
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
        this.workloadService = workloadService;
        this.workloadSuggestionService = workloadSuggestionService;
        this.userAccountService = userAccountService;
        this.jobPostingService = jobPostingService;
        this.showLoginFrame = showLoginFrame;

        setTitle("Admin Dashboard");
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
        root.add(createMainContent(), BorderLayout.CENTER);
        setContentPane(root);

        refreshWorkload();
        refreshAccounts();
        refreshInvigilationJobs();
    }

    private void styleFields() {
        UiTheme.styleInput(searchField);
        UiTheme.styleInput(postingTitleField);
        UiTheme.styleInput(postingModuleField);
        UiTheme.styleInput(postingHoursField);
        UiTheme.styleInput(postingDeadlineField);
        UiTheme.styleInput(examDateField);
        UiTheme.styleInput(examTimeField);
        UiTheme.styleInput(locationField);
        UiTheme.styleInput(invigilatorsField);
        UiTheme.styleTextArea(postingDescriptionArea);
        UiTheme.styleTextArea(postingSkillsArea);
        UiTheme.styleTextArea(workloadSuggestionArea);
        workloadSuggestionArea.setEditable(false);
        workloadSuggestionArea.setRows(10);
        UiTheme.styleInput(suggestionSelector);
    }

    private void installInteractions() {
        workloadTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workloadTable.setDefaultRenderer(Object.class, new OverloadRenderer());
        workloadTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = workloadTable.getSelectedRow();
                showSelectedRow(row >= 0 && row < currentRows.size() ? currentRows.get(row) : null);
            }
        });

        accountTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = accountTable.getSelectedRow();
                showSelectedAccount(row >= 0 && row < currentAccounts.size() ? currentAccounts.get(row) : null);
            }
        });

        deactivateButton.addActionListener(e -> deactivateSelectedAccount());
        deactivateButton.setEnabled(false);
        suggestionSelector.addActionListener(e -> showSelectedSuggestion());
        dismissSuggestionButton.addActionListener(e -> dismissSelectedSuggestion());
        focusSuggestionButton.addActionListener(e -> focusSelectedSuggestionTa());

        bindLiveRefresh(searchField);
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

        JLabel badge = new JLabel("Admin");
        badge.setFont(UiTheme.SECTION_FONT);
        badge.setAlignmentX(LEFT_ALIGNMENT);
        sidebar.add(badge);
        sidebar.add(Box.createVerticalStrut(16));

        JButton workloadButton = UiTheme.navButton("Dashboard");
        workloadButton.addActionListener(e -> showCard(CARD_WORKLOAD));
        sidebar.add(workloadButton);

        JButton userButton = UiTheme.navButton("User Accounts");
        userButton.addActionListener(e -> showCard(CARD_USERS));
        sidebar.add(userButton);

        JButton postingButton = UiTheme.navButton("Job Posting");
        postingButton.addActionListener(e -> showCard(CARD_POSTING));
        sidebar.add(postingButton);

        JButton reportsButton = UiTheme.navButton("Reports");
        reportsButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Report export is out of scope for this release."));
        sidebar.add(reportsButton);

        sidebar.add(Box.createVerticalGlue());
        JButton backButton = UiTheme.secondaryButton("Back to Login");
        backButton.addActionListener(e -> returnToLogin());
        sidebar.add(backButton);
        return sidebar;
    }

    private JPanel createMainContent() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(UiTheme.APP_BACKGROUND);
        panel.setBorder(UiTheme.pagePadding());
        panel.add(createTopBar(), BorderLayout.NORTH);

        contentPanel.setOpaque(false);
        contentPanel.add(createWorkloadCard(), CARD_WORKLOAD);
        contentPanel.add(createUserAccountsCard(), CARD_USERS);
        contentPanel.add(createInvigilationPostingCard(), CARD_POSTING);
        panel.add(contentPanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setBackground(UiTheme.APP_BACKGROUND);
        topBar.add(searchField, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        right.add(new JLabel("Admin User"));
        right.add(new JLabel(adminEmail.isBlank() ? "admin@g30.local" : adminEmail));
        topBar.add(right, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createWorkloadCard() {
        JPanel card = new JPanel(new BorderLayout(18, 18));
        card.setBackground(UiTheme.PANEL_BACKGROUND);
        card.setBorder(UiTheme.cardBorder());

        JLabel title = new JLabel("TA Weekly Hours and Allocation");
        title.setFont(UiTheme.SECTION_FONT);
        card.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(18, 18));
        center.setOpaque(false);
        center.add(createMetricStrip(), BorderLayout.NORTH);
        center.add(createWorkloadSplit(), BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);
        return card;
    }

    private JPanel createMetricStrip() {
        JPanel metrics = new JPanel(new GridLayout(1, 3, 18, 18));
        metrics.setOpaque(false);
        metrics.add(buildMetricCard("Max Weekly Hours", thresholdValue));
        metrics.add(buildMetricCard("Overloaded TAs", overloadedCountValue));
        metrics.add(buildMetricCard("Tracked TAs", taCountValue));
        return metrics;
    }

    private JPanel buildMetricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(UiTheme.SIDEBAR_BACKGROUND);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        valueLabel.setFont(UiTheme.SECTION_FONT);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JSplitPane createWorkloadSplit() {
        JScrollPane tablePane = new JScrollPane(workloadTable);
        tablePane.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));

        JPanel sidePanel = new JPanel(new GridLayout(2, 1, 12, 12));
        sidePanel.setOpaque(false);

        JPanel detailPanel = new JPanel(new BorderLayout(12, 12));
        detailPanel.setBackground(UiTheme.PANEL_BACKGROUND);
        detailPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));

        selectedTaLabel.setFont(UiTheme.SECTION_FONT);
        detailPanel.add(selectedTaLabel, BorderLayout.NORTH);
        detailPanel.add(new JScrollPane(selectedTaDetail), BorderLayout.CENTER);

        JButton refreshButton = UiTheme.primaryButton("Refresh Data");
        refreshButton.addActionListener(e -> refreshWorkload());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(refreshButton);
        detailPanel.add(footer, BorderLayout.SOUTH);

        JPanel suggestionPanel = new JPanel(new BorderLayout(12, 12));
        suggestionPanel.setBackground(UiTheme.PANEL_BACKGROUND);
        suggestionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UiTheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14)
        ));
        JLabel suggestionTitle = new JLabel("AI Workload Balancing Suggestions");
        suggestionTitle.setFont(UiTheme.SECTION_FONT);
        JPanel suggestionHeader = new JPanel(new BorderLayout(8, 8));
        suggestionHeader.setOpaque(false);
        suggestionHeader.add(suggestionTitle, BorderLayout.WEST);
        suggestionHeader.add(suggestionSelector, BorderLayout.CENTER);
        suggestionPanel.add(suggestionHeader, BorderLayout.NORTH);
        suggestionPanel.add(new JScrollPane(workloadSuggestionArea), BorderLayout.CENTER);

        JPanel suggestionActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        suggestionActions.setOpaque(false);
        suggestionActions.add(dismissSuggestionButton);
        suggestionActions.add(focusSuggestionButton);
        suggestionPanel.add(suggestionActions, BorderLayout.SOUTH);

        sidePanel.add(detailPanel);
        sidePanel.add(suggestionPanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePane, sidePanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setBorder(null);
        return splitPane;
    }

    private JPanel createUserAccountsCard() {
        JPanel left = new JPanel(new BorderLayout(12, 12));
        left.setBackground(UiTheme.PANEL_BACKGROUND);
        left.setBorder(UiTheme.cardBorder());
        JLabel title = new JLabel("User Accounts");
        title.setFont(UiTheme.SECTION_FONT);
        left.add(title, BorderLayout.NORTH);
        left.add(new JScrollPane(accountTable), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(12, 12));
        right.setBackground(UiTheme.PANEL_BACKGROUND);
        right.setBorder(UiTheme.cardBorder());
        selectedAccountLabel.setFont(UiTheme.SECTION_FONT);
        right.add(selectedAccountLabel, BorderLayout.NORTH);
        right.add(new JScrollPane(selectedAccountDetail), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        actions.setOpaque(false);
        actions.add(deactivateButton);
        right.add(actions, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPane.setResizeWeight(0.58);
        splitPane.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(splitPane, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createInvigilationPostingCard() {
        JPanel card = new JPanel(new BorderLayout(18, 18));
        card.setBackground(UiTheme.PANEL_BACKGROUND);
        card.setBorder(UiTheme.cardBorder());

        JLabel title = new JLabel("Post an Invigilation Job");
        title.setFont(UiTheme.TITLE_FONT);
        card.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(1, 2, 18, 18));
        form.setOpaque(false);

        JPanel left = UiTheme.transparentPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(createFieldBlock("Job Title", postingTitleField));
        left.add(UiTheme.verticalGap(14));
        left.add(createFieldBlock("Module Code", postingModuleField));
        left.add(UiTheme.verticalGap(14));
        left.add(createFieldBlock("Hours per week", postingHoursField));
        left.add(UiTheme.verticalGap(14));
        left.add(createFieldBlock("Application Deadline (yyyy-MM-dd)", postingDeadlineField));
        left.add(UiTheme.verticalGap(14));
        left.add(createFieldBlock("Exam Date (yyyy-MM-dd)", examDateField));

        JPanel right = UiTheme.transparentPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.add(createFieldBlock("Exam Time", examTimeField));
        right.add(UiTheme.verticalGap(14));
        right.add(createFieldBlock("Location", locationField));
        right.add(UiTheme.verticalGap(14));
        right.add(createFieldBlock("Invigilators Needed", invigilatorsField));
        right.add(UiTheme.verticalGap(14));
        right.add(createTextAreaBlock("Required Skills", postingSkillsArea));
        right.add(UiTheme.verticalGap(14));
        right.add(createTextAreaBlock("Job Description", postingDescriptionArea));

        form.add(left);
        form.add(right);

        JPanel center = new JPanel(new BorderLayout(18, 18));
        center.setOpaque(false);
        center.add(form, BorderLayout.NORTH);
        center.add(new JScrollPane(invigilationTable), BorderLayout.CENTER);
        card.add(center, BorderLayout.CENTER);

        JButton publishButton = UiTheme.primaryButton("Publish Invigilation Job");
        publishButton.addActionListener(e -> publishInvigilationJob());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(publishButton);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private void showCard(String cardName) {
        activeCard = cardName;
        cardLayout.show(contentPanel, cardName);
        switch (cardName) {
            case CARD_WORKLOAD -> refreshWorkload();
            case CARD_USERS -> refreshAccounts();
            case CARD_POSTING -> refreshInvigilationJobs();
            default -> {
            }
        }
    }

    private void refreshWorkload() {
        String query = normalizeSearch(searchField.getText());
        currentRows.clear();
        workloadModel.setRowCount(0);

        int overloadedCount = 0;
        for (TaWorkloadSummary row : workloadService.buildSummary()) {
            if (!matchesWorkloadQuery(row, query)) {
                continue;
            }
            currentRows.add(row);
            if (row.overloaded()) {
                overloadedCount++;
            }
            workloadModel.addRow(new Object[]{
                    row.fullName(),
                    row.studentId(),
                    row.assignedModules().isBlank() ? "-" : row.assignedModules(),
                    row.totalWeeklyHours(),
                    row.overloaded() ? "Above Threshold" : "Within Limit"
            });
        }

        thresholdValue.setText(Integer.toString(workloadService.maxWeeklyHours()));
        overloadedCountValue.setText(Integer.toString(overloadedCount));
        taCountValue.setText(Integer.toString(currentRows.size()));
        refreshSuggestions();

        if (!currentRows.isEmpty()) {
            workloadTable.setRowSelectionInterval(0, 0);
        } else {
            showSelectedRow(null);
        }
        workloadTable.repaint();
    }

    private boolean matchesWorkloadQuery(TaWorkloadSummary row, String query) {
        if (query.isBlank()) {
            return true;
        }
        return containsIgnoreCase(row.fullName(), query)
                || containsIgnoreCase(row.studentId(), query)
                || containsIgnoreCase(row.assignedModules(), query)
                || containsIgnoreCase(row.taEmail(), query);
    }

    private void showSelectedRow(TaWorkloadSummary row) {
        if (row == null) {
            selectedTaLabel.setText("Select a TA");
            selectedTaDetail.setText("No TA workload data is available yet.");
            return;
        }

        selectedTaLabel.setText(row.fullName());
        selectedTaDetail.setText(
                "Email: " + row.taEmail() + System.lineSeparator()
                        + "Student ID: " + blankFallback(row.studentId()) + System.lineSeparator()
                        + "Assigned Modules: " + blankFallback(row.assignedModules()) + System.lineSeparator()
                        + "Total Weekly Hours: " + row.totalWeeklyHours() + System.lineSeparator()
                        + "Threshold Status: " + (row.overloaded() ? "Above configured limit" : "Within configured limit")
        );
    }

    private void refreshAccounts() {
        String query = normalizeSearch(searchField.getText());
        currentAccounts.clear();
        accountModel.setRowCount(0);
        for (ManagedUserAccount account : userAccountService.listManageableAccounts()) {
            if (!matchesAccountQuery(account, query)) {
                continue;
            }
            currentAccounts.add(account);
            accountModel.addRow(new Object[]{
                    account.displayName(),
                    account.email(),
                    account.role(),
                    account.status(),
                    account.studentId().isBlank() ? "-" : account.studentId()
            });
        }
        if (!currentAccounts.isEmpty()) {
            accountTable.setRowSelectionInterval(0, 0);
        } else {
            showSelectedAccount(null);
        }
    }

    private boolean matchesAccountQuery(ManagedUserAccount account, String query) {
        if (query.isBlank()) {
            return true;
        }
        return containsIgnoreCase(account.displayName(), query)
                || containsIgnoreCase(account.email(), query)
                || containsIgnoreCase(account.role(), query)
                || containsIgnoreCase(account.status(), query)
                || containsIgnoreCase(account.studentId(), query);
    }

    private void showSelectedAccount(ManagedUserAccount account) {
        if (account == null) {
            selectedAccountLabel.setText("Select an account");
            selectedAccountDetail.setText("Choose a TA or MO account to review its current status.");
            deactivateButton.setEnabled(false);
            return;
        }
        selectedAccountLabel.setText(account.displayName());
        selectedAccountDetail.setText(
                "Email: " + account.email() + System.lineSeparator()
                        + "Role: " + account.role() + System.lineSeparator()
                        + "Status: " + account.status() + System.lineSeparator()
                        + "Student ID: " + blankFallback(account.studentId()) + System.lineSeparator()
                        + "Last Updated: " + blankFallback(account.updatedAt())
        );
        deactivateButton.setEnabled("ACTIVE".equalsIgnoreCase(account.status()));
    }

    private void deactivateSelectedAccount() {
        int row = accountTable.getSelectedRow();
        if (row < 0 || row >= currentAccounts.size()) {
            JOptionPane.showMessageDialog(this, "Please select an account first.");
            return;
        }
        ManagedUserAccount account = currentAccounts.get(row);
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Deactivate " + account.email() + "?",
                "Confirm Deactivation",
                JOptionPane.YES_NO_OPTION
        );
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            userAccountService.deactivateAccount(account.email());
            refreshAccounts();
            JOptionPane.showMessageDialog(this, "Account deactivated.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "Unable to deactivate account: " + ex.getMessage());
        }
    }

    private void publishInvigilationJob() {
        try {
            jobPostingService.postJob(new JobPostingDraft(
                    adminEmail,
                    postingTitleField.getText(),
                    postingModuleField.getText(),
                    postingDescriptionArea.getText(),
                    postingSkillsArea.getText(),
                    Integer.parseInt(postingHoursField.getText().trim()),
                    LocalDate.parse(postingDeadlineField.getText().trim()),
                    "INVIGILATION",
                    LocalDate.parse(examDateField.getText().trim()),
                    examTimeField.getText(),
                    locationField.getText(),
                    Integer.parseInt(invigilatorsField.getText().trim())
            ));
            clearInvigilationForm();
            refreshInvigilationJobs();
            JOptionPane.showMessageDialog(this, "Invigilation job posted.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Posting failed: " + ex.getMessage());
        }
    }

    private void refreshInvigilationJobs() {
        invigilationModel.setRowCount(0);
        for (JobPosting posting : jobPostingService.viewPostingsByOwner(adminEmail)) {
            if (!posting.isInvigilation()) {
                continue;
            }
            invigilationModel.addRow(new Object[]{
                    posting.title(),
                    posting.moduleCode(),
                    posting.examDate() == null ? "-" : posting.examDate().toString(),
                    blankFallback(posting.location()),
                    posting.status()
            });
        }
    }

    private void clearInvigilationForm() {
        postingTitleField.setText("");
        postingModuleField.setText("");
        postingHoursField.setText("");
        postingDeadlineField.setText("");
        examDateField.setText("");
        examTimeField.setText("");
        locationField.setText("");
        invigilatorsField.setText("");
        postingDescriptionArea.setText("");
        postingSkillsArea.setText("");
    }

    private String normalizeSearch(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private boolean containsIgnoreCase(String source, String query) {
        return source != null && source.toLowerCase().contains(query);
    }

    private String blankFallback(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private JTextArea buildReadonlyTextArea() {
        JTextArea area = new JTextArea();
        UiTheme.styleTextArea(area);
        area.setEditable(false);
        return area;
    }

    private void bindLiveRefresh(JTextField field) {
        field.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                refreshActiveCard();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                refreshActiveCard();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                refreshActiveCard();
            }
        });
    }

    private void refreshActiveCard() {
        switch (activeCard) {
            case CARD_WORKLOAD -> refreshWorkload();
            case CARD_USERS -> refreshAccounts();
            default -> {
            }
        }
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

    private void returnToLogin() {
        if (returningToLogin) {
            return;
        }
        returningToLogin = true;
        dispose();
        showLoginFrame.run();
    }

    private void refreshSuggestions() {
        List<WorkloadSuggestion> suggestions = workloadSuggestionService.buildSuggestions();
        currentSuggestions.clear();
        suggestionSelector.removeAllItems();
        for (WorkloadSuggestion suggestion : suggestions) {
            if (dismissedSuggestionKeys.contains(suggestionKey(suggestion))) {
                continue;
            }
            currentSuggestions.add(suggestion);
            suggestionSelector.addItem(suggestion);
        }
        if (!currentSuggestions.isEmpty()) {
            suggestionSelector.setSelectedIndex(0);
        } else {
            workloadSuggestionArea.setText("No workload suggestions available.");
        }
        dismissSuggestionButton.setEnabled(!currentSuggestions.isEmpty());
        focusSuggestionButton.setEnabled(!currentSuggestions.isEmpty() && currentSuggestions.stream().anyMatch(WorkloadSuggestion::actionable));
    }

    private void showSelectedSuggestion() {
        WorkloadSuggestion suggestion = (WorkloadSuggestion) suggestionSelector.getSelectedItem();
        if (suggestion == null) {
            workloadSuggestionArea.setText("No workload suggestions available.");
            dismissSuggestionButton.setEnabled(false);
            focusSuggestionButton.setEnabled(false);
            return;
        }
        workloadSuggestionArea.setText(
                suggestion.title() + System.lineSeparator() + System.lineSeparator() + suggestion.detail()
        );
        dismissSuggestionButton.setEnabled(true);
        focusSuggestionButton.setEnabled(suggestion.actionable() && !suggestion.overloadedTaEmail().isBlank());
    }

    private void dismissSelectedSuggestion() {
        WorkloadSuggestion suggestion = (WorkloadSuggestion) suggestionSelector.getSelectedItem();
        if (suggestion == null) {
            return;
        }
        dismissedSuggestionKeys.add(suggestionKey(suggestion));
        refreshSuggestions();
    }

    private void focusSelectedSuggestionTa() {
        WorkloadSuggestion suggestion = (WorkloadSuggestion) suggestionSelector.getSelectedItem();
        if (suggestion == null || suggestion.overloadedTaEmail().isBlank()) {
            return;
        }
        showCard(CARD_WORKLOAD);
        for (int i = 0; i < currentRows.size(); i++) {
            if (currentRows.get(i).taEmail().equalsIgnoreCase(suggestion.overloadedTaEmail())) {
                workloadTable.setRowSelectionInterval(i, i);
                workloadTable.scrollRectToVisible(workloadTable.getCellRect(i, 0, true));
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "The referenced TA is not visible in the current workload table.");
    }

    private String suggestionKey(WorkloadSuggestion suggestion) {
        return suggestion.overloadedTaEmail() + "|" + suggestion.suggestedCandidateEmail() + "|" + suggestion.suggestedJobId() + "|" + suggestion.title();
    }

    private final class OverloadRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column
        ) {
            Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            boolean overloaded = row >= 0 && row < currentRows.size() && currentRows.get(row).overloaded();
            if (isSelected) {
                component.setBackground(new Color(221, 234, 248));
            } else if (overloaded) {
                component.setBackground(new Color(255, 237, 237));
            } else {
                component.setBackground(Color.WHITE);
            }
            return component;
        }
    }
}
