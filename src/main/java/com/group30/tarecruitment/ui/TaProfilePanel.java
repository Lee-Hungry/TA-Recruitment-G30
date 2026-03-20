package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.profile.TaProfileService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;

public class TaProfilePanel extends JPanel {

    public TaProfilePanel(TaProfileService profileService) {
        setLayout(new GridLayout(8, 2, 8, 8));

        JTextField userId = new JTextField();
        JTextField fullName = new JTextField();
        JTextField studentId = new JTextField();
        JTextField degree = new JTextField();
        JTextField gpa = new JTextField();
        JTextField skills = new JTextField();
        JTextField availability = new JTextField();
        JButton save = new JButton("Save Profile");

        add(new JLabel("User ID"));
        add(userId);
        add(new JLabel("Full Name"));
        add(fullName);
        add(new JLabel("Student ID"));
        add(studentId);
        add(new JLabel("Degree"));
        add(degree);
        add(new JLabel("GPA"));
        add(gpa);
        add(new JLabel("Skills"));
        add(skills);
        add(new JLabel("Availability"));
        add(availability);
        add(new JLabel(""));
        add(save);

        save.addActionListener(e -> {
            try {
                profileService.createFirstProfile(
                        userId.getText().trim(),
                        fullName.getText().trim(),
                        studentId.getText().trim(),
                        degree.getText().trim(),
                        gpa.getText().trim(),
                        skills.getText().trim(),
                        availability.getText().trim()
                );
                JOptionPane.showMessageDialog(this, "Profile saved.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
    }
}
