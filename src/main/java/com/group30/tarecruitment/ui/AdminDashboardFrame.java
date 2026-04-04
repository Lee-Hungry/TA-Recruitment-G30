package com.group30.tarecruitment.ui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AdminDashboardFrame extends JFrame {

    private boolean returningToLogin;

    public AdminDashboardFrame(String adminEmail, Runnable showLoginFrame) {
        setTitle("Admin Dashboard");
        setSize(1280, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                returnToLogin(showLoginFrame);
            }
        });

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UiTheme.APP_BACKGROUND);
        root.add(createSidebar(showLoginFrame), BorderLayout.WEST);
        root.add(createMainContent(adminEmail), BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel createSidebar(Runnable showLoginFrame) {
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
        sidebar.add(UiTheme.navButton("Dashboard"));
        sidebar.add(UiTheme.navButton("TAs"));
        sidebar.add(UiTheme.navButton("Departments"));
        sidebar.add(UiTheme.navButton("Job Posting"));
        sidebar.add(UiTheme.navButton("Reports"));
        sidebar.add(Box.createVerticalGlue());

        JButton backButton = UiTheme.secondaryButton("Back to Login");
        backButton.addActionListener(e -> returnToLogin(showLoginFrame));
        sidebar.add(backButton);
        return sidebar;
    }

    private JPanel createMainContent(String adminEmail) {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(UiTheme.APP_BACKGROUND);
        panel.setBorder(UiTheme.pagePadding());
        panel.add(createTopBar(adminEmail), BorderLayout.NORTH);
        panel.add(createDashboardCard(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTopBar(String adminEmail) {
        JPanel topBar = new JPanel(new BorderLayout(12, 12));
        topBar.setBackground(UiTheme.APP_BACKGROUND);

        JTextField searchField = new JTextField("Search TAs, modules...");
        UiTheme.styleInput(searchField);
        topBar.add(searchField, BorderLayout.CENTER);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);
        right.add(new JLabel("Notifications"));
        right.add(new JLabel(adminEmail));
        topBar.add(right, BorderLayout.EAST);
        return topBar;
    }

    private JPanel createDashboardCard() {
        JPanel card = new JPanel(new BorderLayout(18, 18));
        card.setBackground(UiTheme.PANEL_BACKGROUND);
        card.setBorder(UiTheme.cardBorder());

        JLabel title = new JLabel("TA Weekly Hours and Allocation");
        title.setFont(UiTheme.SECTION_FONT);
        card.add(title, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridLayout(1, 2, 18, 18));
        body.setOpaque(false);

        DefaultTableModel model = new DefaultTableModel(
                new Object[]{"TA Name", "Department", "Module", "Weekly Hours", "Overload Status"},
                0
        );
        JScrollPane tablePane = new JScrollPane(new JTable(model));
        tablePane.setBorder(BorderFactory.createLineBorder(UiTheme.BORDER_COLOR));
        body.add(tablePane);

        JPanel suggestion = new JPanel(new BorderLayout());
        suggestion.setBackground(UiTheme.PANEL_BACKGROUND);
        suggestion.setBorder(UiTheme.cardBorder());
        suggestion.setPreferredSize(new Dimension(280, 0));
        JLabel message = new JLabel("<html><body style='width:220px'>"
                + "Admin login is complete for Sprint 1. Workload analytics and recommendations arrive in later sprints."
                + "</body></html>");
        message.setFont(UiTheme.BODY_FONT);
        suggestion.add(message, BorderLayout.CENTER);
        body.add(suggestion);

        card.add(body, BorderLayout.CENTER);
        return card;
    }

    private void returnToLogin(Runnable showLoginFrame) {
        if (returningToLogin) {
            return;
        }
        returningToLogin = true;
        dispose();
        showLoginFrame.run();
    }
}
