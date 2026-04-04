package com.group30.tarecruitment.ui;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class DashboardFrame extends JFrame {

    public DashboardFrame(String titleText) {
        setTitle(titleText);
        setSize(420, 220);
        setLocationRelativeTo(null);
        add(new JLabel(titleText + " loaded.", JLabel.CENTER));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }
}
