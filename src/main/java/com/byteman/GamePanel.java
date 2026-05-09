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
    private ArrayList<EnemyBullet> enemyBullets = new ArrayList<>();
    private ArrayList<Enemy> enemies = new ArrayList<>();
    private ArrayList<Platform> platforms = new ArrayList<>();
    private ArrayList<PowerUp> powerUps = new ArrayList<>();
    private Timer gameLoop;
    private BufferedImage backgroundImage;
    private Random rand = new Random();

    // BUFF TIMERS
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
        bullets.clear(); enemyBullets.clear(); enemies.clear(); platforms.clear(); powerUps.clear();
        rapidFireTimer = 0; slowMoTimer = 0;

        player.resetPosition(50, GameConstants.FLOOR_Y - 100);

        // --- ORGANIC / RANDOMIZED STAIRCASE GENERATION ---
        int numPlatforms = 5 + (level / 2);
        int currentX = 100 + rand.nextInt(300); // Start somewhere roughly on the left side
        int currentY = GameConstants.FLOOR_Y - 90;

        for (int i = 0; i < numPlatforms; i++) {
            int platWidth = 80 + rand.nextInt(80); // Random width between 80 and 160
            platforms.add(new Platform(currentX, currentY, platWidth, 20));

            // Move up a random amount (keeps the staircase feel)
            currentY -= (70 + rand.nextInt(40));

            // Randomly jump left or right for the next platform
            int jumpDistance = 60 + rand.nextInt(120);
            if (rand.nextBoolean()) {
                currentX += jumpDistance; // Build to the right
            } else {
                currentX -= jumpDistance; // Build to the left
            }

            // Boundary checks: force them to bounce back if they hit the edge of the screen
            if (currentX < 30) {
                currentX = 30 + rand.nextInt(50);
            }
            if (currentX + platWidth > GameConstants.WIDTH - 30) {
                currentX = GameConstants.WIDTH - platWidth - 30 - rand.nextInt(50);
            }

            if (currentY < 100) break; // Don't build off the top of the screen
        }

        // SPAWN POWER UP
        if (rand.nextBoolean() && platforms.size() > 0) {
            Platform targetPlat = platforms.get(rand.nextInt(platforms.size()));
            PowerUp.Type type = rand.nextBoolean() ? PowerUp.Type.RAPID_FIRE : PowerUp.Type.SLOW_MOTION;
            powerUps.add(new PowerUp(targetPlat.getBounds().x + 10, targetPlat.getBounds().y - 32, type));
        }

        // ENEMY SPAWNS (Easy to Hard)
        int numPlatformEnemies = Math.min(platforms.size() - 1, (level + 1) / 2);

        for (int i = 1; i <= numPlatformEnemies; i++) {
            Platform p = platforms.get(i);
            Rectangle bounds = p.getBounds();
            int speed = 2 + (level / 4);
            enemies.add(new Enemy(bounds.x, bounds.y - 48, bounds.width - 48, speed));
        }

        // Add extra ground troops ONLY starting from level 3
        if (level >= 3) {
            int groundEnemies = level / 3;
            for (int i = 0; i < groundEnemies; i++) {
                int startX = 200 + rand.nextInt(400);
                enemies.add(new Enemy(startX, GameConstants.FLOOR_Y - 48, 200, 2 + (level / 5)));
            }
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
                if (gameState == State.PLAYING) { player.shoot(bullets, rapidFireTimer > 0); }
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter");
        am.put("enter", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (gameState == State.DEAD) { loadLevel(currentLevel); gameState = State.PLAYING; }
                else if (gameState == State.GAME_OVER || gameState == State.GAME_WON) {
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
                for (Enemy enemy : enemies) enemy.update(player, enemyBullets);

                Iterator<EnemyBullet> ebIt = enemyBullets.iterator();
                while (ebIt.hasNext()) {
                    EnemyBullet eb = ebIt.next();
                    eb.update();
                    if (eb.isOffScreen()) ebIt.remove();
                }
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
                killPlayer(); return;
            }
        }

        Iterator<EnemyBullet> ebIt = enemyBullets.iterator();
        while (ebIt.hasNext()) {
            if (pRect.intersects(ebIt.next().getBounds())) {
                ebIt.remove(); killPlayer(); return;
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

    private void killPlayer() {
        player.lives--;
        if (player.lives > 0) gameState = State.DEAD;
        else gameState = State.GAME_OVER;
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
        for (EnemyBullet eb : enemyBullets) eb.draw(g2d);
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

        g2d.setColor(new Color(0, 0, 0, 220));
        g2d.fillRect(0, 0, GameConstants.WIDTH, GameConstants.HEIGHT);
        g2d.setColor(Color.WHITE);

        int centerX = GameConstants.WIDTH / 2;
        int centerY = GameConstants.HEIGHT / 2;

        if (gameState == State.PAUSED) {
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("PAUSED", centerX - 100, centerY - 130);

            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Press ESC to Resume", centerX - 100, centerY - 80);
            g2d.setColor(Color.GREEN);
            g2d.drawString("Press S to Save Game", centerX - 100, centerY - 50);
            g2d.setColor(Color.RED);
            g2d.drawString("Press Q to Quit to Menu", centerX - 100, centerY - 20);

            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 22));
            g2d.drawString("--- HOW TO PLAY ---", centerX - 110, centerY + 40);

            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Left / Right Arrows: Move  |  Up Arrow: Jump  |  Z: Shoot", centerX - 225, centerY + 70);

            g2d.setColor(Color.ORANGE);
            g2d.drawString("\u25A0 Gold Cube: RAPID FIRE (10s Infinite Ammo)", centerX - 180, centerY + 110);

            g2d.setColor(Color.CYAN);
            g2d.drawString("\u25A0 Cyan Cube: SLOW MOTION (10s Slower Enemies)", centerX - 195, centerY + 140);

        } else if (gameState == State.DEAD) {
            g2d.setFont(new Font("Arial", Font.BOLD, 40)); g2d.drawString("YOU DIED", centerX - 100, centerY - 20);
            g2d.setFont(new Font("Arial", Font.PLAIN, 24)); g2d.drawString("Press ENTER to Respawn", centerX - 140, centerY + 30);
        } else if (gameState == State.GAME_OVER) {
            g2d.setColor(Color.RED); g2d.setFont(new Font("Arial", Font.BOLD, 60)); g2d.drawString("GAME OVER", centerX - 190, centerY - 20);
            g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.drawString("Final Score: " + player.score, centerX - 100, centerY + 20);
            g2d.drawString("Press ENTER to Try Again", centerX - 145, centerY + 60);
        } else if (gameState == State.GAME_WON) {
            g2d.setColor(Color.GREEN); g2d.setFont(new Font("Arial", Font.BOLD, 60)); g2d.drawString("YOU WIN!", centerX - 150, centerY - 20);
            g2d.setColor(Color.WHITE); g2d.setFont(new Font("Arial", Font.PLAIN, 24));
            g2d.drawString("Final Score: " + player.score, centerX - 100, centerY + 20);
            g2d.drawString("Press ENTER to Play Again", centerX - 145, centerY + 60);
        }
    }
}