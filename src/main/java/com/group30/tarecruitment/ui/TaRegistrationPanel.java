package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.registration.TaRegistrationRequest;
import com.group30.tarecruitment.registration.TaRegistrationService;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.GridLayout;

public class TaRegistrationPanel extends JPanel {

    public TaRegistrationPanel(TaRegistrationService service) {
        setLayout(new GridLayout(5, 2, 8, 8));

        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField studentIdField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JButton submit = new JButton("Register");

        add(new JLabel("Full Name"));
        add(fullNameField);
        add(new JLabel("Email"));
        add(emailField);
        add(new JLabel("Student ID"));
        add(studentIdField);
        add(new JLabel("Password"));
        add(passwordField);
        add(new JLabel(""));
        add(submit);

        submit.addActionListener(e -> {
            TaRegistrationRequest request = new TaRegistrationRequest(
                    fullNameField.getText().trim(),
                    emailField.getText().trim(),
                    studentIdField.getText().trim(),
                    new String(passwordField.getPassword())
            );
            try {
                service.register(request);
                JOptionPane.showMessageDialog(this, "Registration completed.");
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        });
    }
}
