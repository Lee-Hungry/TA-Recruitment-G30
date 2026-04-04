package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.auth.AuthResult;
import com.group30.tarecruitment.auth.AuthRole;
import com.group30.tarecruitment.auth.AuthService;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridLayout;

public class LoginFrame extends JFrame {

    private final AuthService authService;

    public LoginFrame(AuthService authService) {
        this.authService = authService;
        setTitle("TA Recruitment Login");
        setSize(480, 260);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridLayout(4, 2, 8, 8));
        JTextField email = new JTextField();
        JPasswordField password = new JPasswordField();
        JComboBox<AuthRole> roleBox = new JComboBox<>(AuthRole.values());
        JButton loginButton = new JButton("Login");

        panel.add(new JLabel("Email"));
        panel.add(email);
        panel.add(new JLabel("Password"));
        panel.add(password);
        panel.add(new JLabel("Role"));
        panel.add(roleBox);
        panel.add(new JLabel(""));
        panel.add(loginButton);

        loginButton.addActionListener(e -> {
            AuthResult result = authService.login(email.getText().trim(),
                    new String(password.getPassword()),
                    (AuthRole) roleBox.getSelectedItem(),
                    "127.0.0.1");
            if (!result.success()) {
                JOptionPane.showMessageDialog(this, "Login failed: " + result.error());
                return;
            }
            route(result.route());
        });

        setContentPane(panel);
    }

    private void route(String route) {
        DashboardFrame dashboardFrame;
        switch (route) {
            case "TA_DASHBOARD" -> dashboardFrame = new DashboardFrame("TA Dashboard");
            case "MO_DASHBOARD" -> dashboardFrame = new DashboardFrame("MO Dashboard");
            case "ADMIN_DASHBOARD" -> dashboardFrame = new DashboardFrame("Admin Dashboard");
            default -> throw new IllegalStateException("Unknown route: " + route);
        }
        dashboardFrame.setVisible(true);
        dispose();
    }
}
