package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.login.TaLoginResult;
import com.group30.tarecruitment.login.TaLoginService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridLayout;

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
                JOptionPane.showMessageDialog(this, "Login success. Session=" + result.sessionId());
                return;
            }
            JOptionPane.showMessageDialog(this, "Login failed: " + result.errorCode());
        });
    }
}
