package com.group30.tarecruitment.ui;

import com.group30.tarecruitment.registration.TaRegistrationRequest;
import com.group30.tarecruitment.registration.TaRegistrationService;

import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

public class TaRegistrationPanel extends JPanel {

    public TaRegistrationPanel(TaRegistrationService service) {
        this(service, () -> {
        });
    }

    public TaRegistrationPanel(TaRegistrationService service, Runnable afterSuccess) {
        setLayout(new BorderLayout());
        setBackground(UiTheme.APP_BACKGROUND);
        setBorder(UiTheme.pagePadding());

        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField studentIdField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        UiTheme.styleInput(fullNameField);
        UiTheme.styleInput(emailField);
        UiTheme.styleInput(studentIdField);
        UiTheme.styleInput(passwordField);

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UiTheme.PANEL_BACKGROUND);
        card.setBorder(UiTheme.cardBorder());

        JLabel title = new JLabel("TA Registration");
        title.setFont(UiTheme.SECTION_FONT);
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("Create your account to enter the recruitment system.");
        subtitle.setFont(UiTheme.BODY_FONT);
        subtitle.setAlignmentX(LEFT_ALIGNMENT);
        card.add(subtitle);
        card.add(Box.createVerticalStrut(24));

        card.add(createFieldBlock("Full Name", fullNameField));
        card.add(UiTheme.verticalGap(14));
        card.add(createFieldBlock("Email", emailField));
        card.add(UiTheme.verticalGap(14));
        card.add(createFieldBlock("Student ID", studentIdField));
        card.add(UiTheme.verticalGap(14));
        card.add(createFieldBlock("Password", passwordField));
        card.add(UiTheme.verticalGap(24));

        JButton submit = UiTheme.primaryButton("Register");
        submit.setMaximumSize(new Dimension(Integer.MAX_VALUE, UiTheme.FIELD_HEIGHT + 8));

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        footer.setOpaque(false);
        footer.add(submit);
        card.add(footer);

        add(card, BorderLayout.CENTER);

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
                afterSuccess.run();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Registration failed: " + ex.getMessage());
            }
        });
    }

    private JPanel createFieldBlock(String labelText, JTextField field) {
        JPanel block = new JPanel();
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));
        block.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(UiTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        label.setAlignmentX(LEFT_ALIGNMENT);
        field.setAlignmentX(LEFT_ALIGNMENT);

        block.add(label);
        block.add(Box.createVerticalStrut(6));
        block.add(field);
        return block;
    }
}
