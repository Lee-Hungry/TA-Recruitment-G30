package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.login.TaLoginService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;

public class TaDashboardFrame extends JFrame {

    public TaDashboardFrame(String sessionId, TaLoginService loginService, Runnable showLoginFrame) {
        setTitle("TA Dashboard");
        setSize(460, 220);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JLabel welcome = new JLabel("Welcome TA. Session=" + sessionId, JLabel.CENTER);
        JButton logout = new JButton("Logout");
        JPanel footer = new JPanel();
        footer.add(logout);

        logout.addActionListener(e -> {
            boolean revoked = loginService.logout(sessionId);
            if (!revoked) {
                JOptionPane.showMessageDialog(this, "Logout failed: session not found.");
                return;
            }
            JOptionPane.showMessageDialog(this, "Logout success.");
            dispose();
            showLoginFrame.run();
        });

        setLayout(new BorderLayout());
        add(welcome, BorderLayout.CENTER);
        add(footer, BorderLayout.SOUTH);
    }
}
