package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.admin.AdminUserAccountService;
import com.group30.tarecruitment.admin.ManagedUserAccount;
import com.group30.tarecruitment.admin.TaWorkloadService;
import com.group30.tarecruitment.admin.TaWorkloadSummary;
import com.group30.tarecruitment.jobs.JobPosting;
import com.group30.tarecruitment.jobs.JobPostingDraft;
import com.group30.tarecruitment.jobs.JobPostingService;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import java.util.List;

public class AdminDashboardFrame extends JFrame {

    private static final String CARD_WORKLOAD = "WORKLOAD";
    private static final String CARD_USERS = "USERS";
    private static final String CARD_POSTING = "POSTING";

    private final String adminEmail;
    private final TaWorkloadService workloadService;
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
    private final List<TaWorkloadSummary> currentRows = new ArrayList<>();
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

    private boolean returningToLogin;
    private String activeCard = CARD_WORKLOAD;

    public AdminDashboardFrame(
            String adminEmail,
            TaWorkloadService workloadService,
            AdminUserAccountService userAccountService,
            JobPostingService jobPostingService,
            Runnable showLoginFrame
    ) {
        this.adminEmail = adminEmail == null ? "" : adminEmail.trim().toLowerCase();
        this.workloadService = workloadService;
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

        UiTheme.styleInput(searchField);
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
        bindLiveRefresh(searchField);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.APP_BACKGROUND);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMainContent(), BorderLayout.CENTER);
        setContentPane(root);

        refreshWorkload();
        refreshAccounts();
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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tablePane, detailPanel);
        splitPane.setResizeWeight(0.7);
        splitPane.setBorder(null);
        return splitPane;
    }

    private void refreshWorkload() {
        currentRows.clear();
        currentRows.addAll(workloadService.buildSummary());
        workloadModel.setRowCount(0);

        int overloadedCount = 0;
        for (TaWorkloadSummary row : currentRows) {
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

        if (!currentRows.isEmpty()) {
            workloadTable.setRowSelectionInterval(0, 0);
        } else {
            showSelectedRow(null);
        }
        workloadTable.repaint();
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

    private String blankFallback(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private JTextArea buildReadonlyTextArea() {
        JTextArea area = new JTextArea();
        UiTheme.styleTextArea(area);
        area.setEditable(false);
        return area;
    }

    private void returnToLogin(Runnable showLoginFrame) {
        if (returningToLogin) {
            return;
        }
        returningToLogin = true;
        dispose();
        showLoginFrame.run();
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
