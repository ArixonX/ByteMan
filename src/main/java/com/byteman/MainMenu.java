package com.byteman;

import javax.swing.*;
import java.awt.*;

public class MainMenu extends JPanel {
    public MainMenu(Main app) {
        setLayout(new GridBagLayout());
        setBackground(new Color(20, 20, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); gbc.gridx = 0;

        JLabel title = new JLabel("BYTEMAN");
        title.setFont(new Font("Arial", Font.BOLD, 50));
        title.setForeground(Color.CYAN); gbc.gridy = 0; add(title, gbc);

        JButton playBtn = createButton("PLAY");
        playBtn.addActionListener(e -> app.startGame()); gbc.gridy = 1; add(playBtn, gbc);

        JButton loadBtn = createButton("LOAD GAME");
        loadBtn.addActionListener(e -> app.loadGame()); gbc.gridy = 2; add(loadBtn, gbc);

        JButton helpBtn = createButton("HELP");
        helpBtn.addActionListener(e -> app.showScreen("HELP")); gbc.gridy = 3; add(helpBtn, gbc);

        JButton quitBtn = createButton("QUIT");
        quitBtn.addActionListener(e -> System.exit(0)); gbc.gridy = 4; add(quitBtn, gbc);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text); btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setBackground(Color.DARK_GRAY); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setPreferredSize(new Dimension(200, 50));
        return btn;
    }
}