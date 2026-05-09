package com.byteman;

import javax.swing.*;
import java.awt.*;

public class HelpMenu extends JPanel {
    public HelpMenu(Main app) {
        setLayout(new GridBagLayout());
        setBackground(new Color(20, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.gridx = 0;

        JLabel title = new JLabel("CONTROLS"); title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setForeground(Color.CYAN); gbc.gridy = 0; add(title, gbc);

        String[] controls = { "Left/Right Arrow - Move", "Up Arrow - Jump", "Z Key - Shoot", "ESC - Pause" };
        for (int i = 0; i < controls.length; i++) {
            JLabel label = new JLabel(controls[i]); label.setFont(new Font("Arial", Font.PLAIN, 20));
            label.setForeground(Color.WHITE); gbc.gridy = i + 1; add(label, gbc);
        }

        JButton backBtn = new JButton("BACK"); backBtn.setFont(new Font("Arial", Font.BOLD, 20));
        backBtn.setBackground(Color.DARK_GRAY); backBtn.setForeground(Color.WHITE);
        backBtn.addActionListener(e -> app.showScreen("MENU"));
        gbc.gridy = controls.length + 1; add(backBtn, gbc);
    }
}