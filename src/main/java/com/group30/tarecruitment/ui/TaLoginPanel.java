package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.login.TaLoginResult;
import com.group30.tarecruitment.login.TaLoginService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.GridLayout;
import java.awt.Window;

public class TaLoginPanel extends JPanel {

    public TaLoginPanel(TaLoginService loginService) {
        setLayout(new GridLayout(3, 2, 8, 8));

        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton loginButton = new JButton("TA Login");

        add(new JLabel("Email"));
        add(emailField);
        add(new JLabel("Password"));
        add(passwordField);
        add(new JLabel(""));
        add(loginButton);

        loginButton.addActionListener(e -> {
            TaLoginResult result = loginService.login(
                    emailField.getText().trim(),
                    new String(passwordField.getPassword()),
                    "127.0.0.1"
            );
            if (result.success()) {
                Window currentWindow = SwingUtilities.getWindowAncestor(this);
                TaDashboardFrame dashboardFrame = new TaDashboardFrame(result.sessionId(), loginService, () -> {
                    JFrame loginFrame = new JFrame("TA Login");
                    loginFrame.setSize(460, 220);
                    loginFrame.setLocationRelativeTo(null);
                    loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    loginFrame.setContentPane(new TaLoginPanel(loginService));
                    loginFrame.setVisible(true);
                });
                dashboardFrame.setVisible(true);
                if (currentWindow != null) {
                    currentWindow.dispose();
                }
                return;
            }
            JOptionPane.showMessageDialog(this, "Login failed: " + result.errorCode());
        });
    }
}
