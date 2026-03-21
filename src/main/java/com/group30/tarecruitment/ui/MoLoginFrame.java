package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.mo.MoLoginResult;
import com.group30.tarecruitment.mo.MoLoginService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridLayout;

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

            new MoDashboardFrame().setVisible(true);
            dispose();
        });

        setContentPane(panel);
    }
}
