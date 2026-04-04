package com.group30.tarecruitment.ui;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class MoDashboardFrame extends JFrame {

    public MoDashboardFrame() {
        setTitle("MO Dashboard");
        setSize(440, 220);
        setLocationRelativeTo(null);
        add(new JLabel("Welcome to MO dashboard", JLabel.CENTER));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
