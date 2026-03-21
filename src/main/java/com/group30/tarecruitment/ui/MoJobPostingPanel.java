package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.job.JobPostingRequest;
import com.group30.tarecruitment.job.JobPostingService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.GridLayout;

public class MoJobPostingPanel extends JPanel {

    public MoJobPostingPanel(JobPostingService postingService) {
        setLayout(new GridLayout(8, 2, 8, 8));

        JTextField createdBy = new JTextField();
        JTextField title = new JTextField();
        JTextField moduleCode = new JTextField();
        JTextField description = new JTextField();
        JTextField skills = new JTextField();
        JTextField hours = new JTextField();
        JTextField deadline = new JTextField();
        JButton submit = new JButton("Publish Job");

        add(new JLabel("MO User ID"));
        add(createdBy);
        add(new JLabel("Title"));
        add(title);
        add(new JLabel("Module Code"));
        add(moduleCode);
        add(new JLabel("Description"));
        add(description);
        add(new JLabel("Required Skills"));
        add(skills);
        add(new JLabel("Hours/Week"));
        add(hours);
        add(new JLabel("Deadline"));
        add(deadline);
        add(new JLabel(""));
        add(submit);

        submit.addActionListener(e -> {
            try {
                int hoursPerWeek;
                try {
                    hoursPerWeek = Integer.parseInt(hours.getText().trim());
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException("INVALID_HOURS_PER_WEEK");
                }
                postingService.publish(new JobPostingRequest(
                        createdBy.getText().trim(),
                        title.getText().trim(),
                        moduleCode.getText().trim(),
                        description.getText().trim(),
                        skills.getText().trim(),
                        hoursPerWeek,
                        deadline.getText().trim(),
                        "TA"
                ));
                JOptionPane.showMessageDialog(this, "Job posted successfully.");
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
    }
}
