package com.group30.tarecruitment.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;

public final class UiTheme {

    public static final Color APP_BACKGROUND = new Color(245, 247, 250);
    public static final Color PANEL_BACKGROUND = Color.WHITE;
    public static final Color SIDEBAR_BACKGROUND = new Color(252, 252, 252);
    public static final Color BORDER_COLOR = new Color(213, 220, 229);
    public static final Color ACCENT = new Color(154, 198, 240);
    public static final Color ACCENT_DARK = new Color(73, 126, 173);
    public static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    public static final Font SECTION_FONT = new Font("SansSerif", Font.BOLD, 18);
    public static final Font BODY_FONT = new Font("SansSerif", Font.PLAIN, 14);
    public static final int FIELD_HEIGHT = 36;
    public static final int TEXT_AREA_HEIGHT = 116;
    public static final int PAGE_GAP = 18;

    private UiTheme() {
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        );
    }

    public static Border pagePadding() {
        return BorderFactory.createEmptyBorder(18, 18, 18, 18);
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ACCENT);
        button.setFont(BODY_FONT.deriveFont(Font.BOLD));
        button.setFocusPainted(false);
        button.setMargin(new Insets(10, 16, 10, 16));
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(Color.WHITE);
        button.setFont(BODY_FONT);
        button.setFocusPainted(false);
        button.setMargin(new Insets(10, 16, 10, 16));
        return button;
    }

    public static JButton navButton(String text) {
        JButton button = new JButton(text);
        button.setHorizontalAlignment(JButton.LEFT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        button.setBackground(Color.WHITE);
        button.setFont(BODY_FONT);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        return button;
    }

    public static void styleTextArea(JTextArea area) {
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(BODY_FONT);
        area.setBackground(Color.WHITE);
    }

    public static void styleInput(JComponent component) {
        component.setFont(BODY_FONT);
        Dimension preferred = component.getPreferredSize();
        component.setPreferredSize(new Dimension(Math.max(preferred.width, 180), FIELD_HEIGHT));
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, FIELD_HEIGHT));
        component.setMinimumSize(new Dimension(120, FIELD_HEIGHT));
    }

    public static JScrollPane createTextAreaScrollPane(JTextArea area) {
        styleTextArea(area);
        JScrollPane scrollPane = new JScrollPane(area);
        scrollPane.setPreferredSize(new Dimension(240, TEXT_AREA_HEIGHT));
        scrollPane.setMinimumSize(new Dimension(180, TEXT_AREA_HEIGHT));
        return scrollPane;
    }

    public static JPanel transparentPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        return panel;
    }

    public static Component verticalGap(int height) {
        return javax.swing.Box.createVerticalStrut(height);
    }

    public static void setPanelBackground(JComponent component) {
        component.setOpaque(true);
        component.setBackground(PANEL_BACKGROUND);
    }
}
