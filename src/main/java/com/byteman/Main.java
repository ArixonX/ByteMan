package com.byteman;

import javax.swing.*;
import java.awt.*;

public class Main extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public Main() {
        setTitle("ByteMan");
        setSize(GameConstants.WIDTH, GameConstants.HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new MainMenu(this), "MENU");
        mainPanel.add(new HelpMenu(this), "HELP");

        add(mainPanel);
        cardLayout.show(mainPanel, "MENU");
    }

    public void showScreen(String screenName) { cardLayout.show(mainPanel, screenName); }

    public void startGame() {
        GamePanel gamePanel = new GamePanel(this);
        mainPanel.add(gamePanel, "GAME");
        cardLayout.show(mainPanel, "GAME");
        gamePanel.requestFocusInWindow();
    }

    public void loadGame() {
        SaveState save = SaveManager.load();
        if (save != null) {
            GamePanel gamePanel = new GamePanel(this);
            gamePanel.loadSaveData(save);
            mainPanel.add(gamePanel, "GAME");
            cardLayout.show(mainPanel, "GAME");
            gamePanel.requestFocusInWindow();
        } else {
            JOptionPane.showMessageDialog(this, "No save file found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }
}