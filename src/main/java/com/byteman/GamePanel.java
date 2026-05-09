package com.byteman;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener {
    private Main app;
    private enum State { PLAYING, PAUSED, DEAD, GAME_OVER, GAME_WON }
    private State gameState = State.PLAYING;
    private int currentLevel = 1;
    private static final int MAX_LEVELS = 10;

    private Player player;
    private ArrayList<Bullet> bullets = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Platform> platforms = new ArrayList<>();
    private ArrayList<PowerUp> powerUps = new ArrayList<>();
    private Timer gameLoop;
    private BufferedImage backgroundImage;
    private Random rand = new Random();

    // BUFF TIMERS (60 frames = 1 second. 600 = 10 seconds)
    private int rapidFireTimer = 0;
    private int slowMoTimer = 0;
    private static final int BUFF_DURATION = 600;
    private int frameCount = 0;

    public GamePanel(Main app) {
        this.app = app;
        setBackground(new Color(20, 20, 30));
        setFocusable(true);
        setupKeyBindings();

        backgroundImage = ImageLoader.load("/bg.png");
        player = new Player(50, 100);
        loadLevel(currentLevel);

        gameLoop = new Timer(16, this);
        gameLoop.start();
    }

    public void loadSaveData(SaveState save) {
        this.currentLevel = save.level;
        this.player.lives = save.lives;
        this.player.currentAmmo = save.ammo;
        this.player.score = save.score;
        loadLevel(currentLevel);
    }

    private void loadLevel(int level) {
        bullets.clear(); enemies.clear(); platforms.clear(); powerUps.clear();
        rapidFireTimer = 0; slowMoTimer = 0;

        player.resetPosition(50, GameConstants.FLOOR_Y - 100);

        int numPlatforms = 4 + rand.nextInt(3);
        int currentX = 100 + rand.nextInt(200);
        int currentY = GameConstants.FLOOR_Y - 80 - rand.nextInt(40);

        for (int i = 0; i < numPlatforms; i++) {
            int platWidth = 80 + rand.nextInt(80);

            if (currentX < 0) currentX = 20;
            if (currentX + platWidth > GameConstants.WIDTH) currentX = GameConstants.WIDTH - platWidth - 20;

            platforms.add(new Platform(currentX, currentY, platWidth, 20));

            currentY -= (60 + rand.nextInt(60));

            int shift = 50 + rand.nextInt(100);
            if (rand.nextBoolean()) currentX += shift;
            else currentX -= shift;

            if (currentY < 100) break;
        }

        // SPAWN A RANDOM POWER UP (50% Chance per level)
        if (rand.nextBoolean() && platforms.size() > 0) {
            Platform targetPlat = platforms.get(rand.nextInt(platforms.size()));
            PowerUp.Type type = rand.nextBoolean() ? PowerUp.Type.RAPID_FIRE : PowerUp.Type.SLOW_MOTION;
            // Spawns 32 pixels above the platform to match the new size
            powerUps.add(new PowerUp(targetPlat.getBounds().x + 10, targetPlat.getBounds().y - 32, type));
        }

        int numEnemies = (level + 1) / 2;
        for (int i = 0; i < numEnemies; i++) {
            int spawnIndex = i % platforms.size();
            Platform p = platforms.get(spawnIndex);
            Rectangle bounds = p.getBounds();
            int eX = bounds.x + (bounds.width / 2) - 25;
            int eY = bounds.y - 50;
            int speed = 2 + (level / 4);
            enemies.add(new Enemy(eX, eY, (bounds.width / 2) - 25, speed));
        }

        if (level >= 3) {
            enemies.add(new Enemy(400, GameConstants.FLOOR_Y - 50, 200, 3 + (level / 5)));
        }
    }

    private void setupKeyBindings() {
        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "left_press");
        am.put("left_press", new AbstractAction() { public void actionPerformed(ActionEvent e) { player.setLeftPressed(true); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "left_release");
        am.put("left_release", new AbstractAction() { public void actionPerformed(ActionEvent e) { player.setLeftPressed(false); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "right_press");
        am.put("right_press", new AbstractAction() { public void actionPerformed(ActionEvent e) { player.setRightPressed(true); } });
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "right_release");
        am.put("right_release", new AbstractAction() { public void actionPerformed(ActionEvent e) { player.setRightPressed(false); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "jump");
        am.put("jump", new AbstractAction() { public void actionPerformed(ActionEvent e) { if (gameState == State.PLAYING) player.jump(); } });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, 0), "shoot");
        am.put("shoot", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (gameState == State.PLAYING) {
                    player.shoot(bullets, rapidFireTimer > 0);
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
        am.put("enter", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (gameState == State.DEAD) {
                    loadLevel(currentLevel); gameState = State.PLAYING;
                } else if (gameState == State.GAME_OVER || gameState == State.GAME_WON) {
                    currentLevel = 1; player.lives = 3; player.currentAmmo = player.maxAmmo; player.score = 0;
                    loadLevel(currentLevel); gameState = State.PLAYING;
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "escape");
        am.put("escape", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (gameState == State.PLAYING) gameState = State.PAUSED;
                else if (gameState == State.PAUSED) gameState = State.PLAYING;
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0), "save");
        am.put("save", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (gameState == State.PAUSED) {
                    SaveManager.save(new SaveState(currentLevel, player.lives, player.currentAmmo, player.score));
                    JOptionPane.showMessageDialog(app, "Game Saved Successfully!");
                }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Q, 0), "quit");
        am.put("quit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (gameState == State.PAUSED) { gameLoop.stop(); app.showScreen("MENU"); }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == State.PLAYING) {
            frameCount++;

            if (rapidFireTimer > 0) rapidFireTimer--;
            if (slowMoTimer > 0) slowMoTimer--;

            player.update(platforms);

            if (slowMoTimer == 0 || frameCount % 2 == 0) {
                for (Enemy enemy : enemies) enemy.update();
            }

            Iterator<Bullet> it = bullets.iterator();
            while (it.hasNext()) {
                Bullet b = it.next();
                b.update();
                if (b.isOffScreen()) it.remove();
            }

            checkCollisions();
            checkLevelTransition();
        }
        repaint();
    }

    private void checkCollisions() {
        Rectangle pRect = player.getBounds();

        Iterator<PowerUp> puIt = powerUps.iterator();
        while (puIt.hasNext()) {
            PowerUp pu = puIt.next();
            if (pRect.intersects(pu.getBounds())) {
                if (pu.getType() == PowerUp.Type.RAPID_FIRE) rapidFireTimer = BUFF_DURATION;
                if (pu.getType() == PowerUp.Type.SLOW_MOTION) slowMoTimer = BUFF_DURATION;
                player.score += 50;
                puIt.remove();
            }
        }

        for (Enemy enemy : enemies) {
            if (pRect.intersects(enemy.getBounds())) {
                player.lives--;
                if (player.lives > 0) gameState = State.DEAD;
                else gameState = State.GAME_OVER;
                return;
            }
        }

        Iterator<Bullet> bulletIt = bullets.iterator();
        while (bulletIt.hasNext()) {
            Bullet b = bulletIt.next();
            Rectangle bRect = b.getBounds();
            Iterator<Enemy> enemyIt = enemies.iterator();
            boolean hit = false;
            while (enemyIt.hasNext()) {
                Enemy enemy = enemyIt.next();
                if (bRect.intersects(enemy.getBounds())) {
                    enemyIt.remove(); hit = true; player.score += 100; break;
                }
            }
            if (hit) bulletIt.remove();
        }
    }

    private void checkLevelTransition() {
        if (player.getX() > GameConstants.WIDTH) {
            currentLevel++;
            player.score += 500;
            if (currentLevel > MAX_LEVELS) gameState = State.GAME_WON;
            else loadLevel(currentLevel);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (backgroundImage != null) g2d.drawImage(backgroundImage, 0, 0, GameConstants.WIDTH, GameConstants.HEIGHT, null);
        else {
            g2d.setColor(Color.GRAY);
            g2d.fillRect(0, GameConstants.FLOOR_Y, GameConstants.WIDTH, GameConstants.HEIGHT - GameConstants.FLOOR_Y);
        }

        g2d.setColor(Color.GREEN);
        g2d.fillRect(GameConstants.WIDTH - 20, GameConstants.FLOOR_Y - 60, 20, 60);

        for (Platform plat : platforms) plat.draw(g2d);
        for (PowerUp pu : powerUps) pu.draw(g2d);
        for (Enemy enemy : enemies) enemy.draw(g2d);
        for (Bullet b : bullets) b.draw(g2d);
        player.draw(g2d);

        drawHUD(g2d);
        drawStateOverlays(g2d);
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("Level: " + currentLevel, 20, 30);
        g2d.setColor(Color.RED); g2d.drawString("Lives: " + player.lives, 20, 60);

        if (rapidFireTimer > 0) {
            g2d.setColor(Color.ORANGE);
            g2d.drawString("Ammo: INFINITE", 20, 90);
        } else {
            g2d.setColor(Color.YELLOW);
            if (player.currentAmmo > 0) g2d.drawString("Ammo: " + player.currentAmmo + "/" + player.maxAmmo, 20, 90);
            else { g2d.setColor(Color.RED); g2d.drawString("RELOADING...", 20, 90); }
        }

        g2d.setColor(Color.CYAN); g2d.drawString("Score: " + player.score, 20, 120);

        if (rapidFireTimer > 0) {
            g2d.setColor(Color.ORANGE);
            g2d.drawString("RAPID FIRE: " + (rapidFireTimer / 60) + "s", 180, 30);
        }
        if (slowMoTimer > 0) {
            g2d.setColor(Color.CYAN);
            g2d.drawString("SLOW MO: " + (slowMoTimer / 60) + "s", 180, 60);
        }
    }

    private void drawStateOverlays(Graphics2D g2d) {
        if (gameState == State.PLAYING) return;

        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, GameConstants.WIDTH, GameConstants.HEIGHT);
        g2d.setColor(Color.WHITE);

        if (gameState == State.PAUSED) {
            g2d.setFont(new Font("Arial", Font.BOLD, 50)); g2d.drawString("PAUSED", GameConstants.WIDTH/2 - 100, GameConstants.HEIGHT/2 - 50);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24)); g2d.drawString("Press ESC to Resume", GameConstants.WIDTH/2 - 120, GameConstants.HEIGHT/2 + 10);
            g2d.setColor(Color.GREEN); g2d.drawString("Press S to Save Game", GameConstants.WIDTH/2 - 120, GameConstants.HEIGHT/2 + 50);
            g2d.setColor(Color.RED); g2d.drawString("Press Q to Quit to Menu", GameConstants.WIDTH/2 - 120, GameConstants.HEIGHT/2 + 90);
        } else if (gameState == State.DEAD) {
            g2d.setFont(new Font("Arial", Font.BOLD, 40)); g2d.drawString("YOU DIED", GameConstants.WIDTH/2 - 100, GameConstants.HEIGHT/2 - 20);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24)); g2d.drawString("Press ENTER to Respawn", GameConstants.WIDTH/2 - 140, GameConstants.HEIGHT/2 + 30);
        } else if (gameState == State.GAME_OVER) {
            g2d.setColor(Color.RED); g2d.setFont(new Font("Arial", Font.BOLD, 60)); g2d.drawString("GAME OVER", GameConstants.WIDTH/2 - 190, GameConstants.HEIGHT/2 - 20);
            g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.drawString("Final Score: " + player.score, GameConstants.WIDTH/2 - 100, GameConstants.HEIGHT/2 + 20);
            g2d.drawString("Press ENTER to Try Again", GameConstants.WIDTH/2 - 145, GameConstants.HEIGHT/2 + 60);
        } else if (gameState == State.GAME_WON) {
            g2d.setColor(Color.GREEN); g2d.setFont(new Font("Arial", Font.BOLD, 60)); g2d.drawString("YOU WIN!", GameConstants.WIDTH/2 - 150, GameConstants.HEIGHT/2 - 20);
            g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.drawString("Final Score: " + player.score, GameConstants.WIDTH/2 - 100, GameConstants.HEIGHT/2 + 20);
            g2d.drawString("Press ENTER to Play Again", GameConstants.WIDTH/2 - 145, GameConstants.HEIGHT/2 + 60);
        }
    }
}