package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.profile.TaProfile;
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
        JButton load = new JButton("Load Profile");
        JButton save = new JButton("Save/Update");

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
        add(load);
        add(save);

        load.addActionListener(e -> {
            try {
                TaProfile profile = profileService.loadByUserId(userId.getText().trim());
                fullName.setText(profile.fullName());
                studentId.setText(profile.studentId());
                studentId.setEditable(false);
                degree.setText(profile.degreeProgramme());
                gpa.setText(profile.gpa());
                skills.setText(profile.skills());
                availability.setText(profile.availability());
                JOptionPane.showMessageDialog(this, "Profile loaded.");
            } catch (IllegalArgumentException ex) {
                studentId.setEditable(true);
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });

        save.addActionListener(e -> {
            try {
                profileService.saveOrUpdate(
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
