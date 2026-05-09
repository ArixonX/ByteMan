package com.byteman;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Enemy {
    private int x, y, startX, walkDistance;
    private int width = 48, height = 48;
    private int speed;

    private int shootTimer = 0;
    private int reactionTimer = 0; // NEW: Takes time to detect the player
    private static final int REACTION_TIME = 30; // 30 frames = 0.5 seconds to react

    private BufferedImage sprite;

    public Enemy(int startX, int y, int walkDistance, int speed) {
        this.x = startX;
        this.startX = startX;
        this.y = y;
        this.walkDistance = walkDistance;
        this.speed = speed;
        this.sprite = ImageLoader.load("/enemy.png");
    }

    public void update(Player player, ArrayList<EnemyBullet> enemyBullets) {
        // Patrol movement
        x += speed;
        if (x < startX || x > startX + walkDistance) {
            speed *= -1; // Turn around
        }

        if (shootTimer > 0) shootTimer--;

        // LINE OF SIGHT LOGIC
        if (Math.abs(player.getY() - this.y) < 60) {
            boolean facingLeft = speed < 0;
            boolean playerIsLeft = player.getX() < this.x;

            // If looking at the player
            if ((facingLeft && playerIsLeft) || (!facingLeft && !playerIsLeft)) {

                // Only start reacting if their gun isn't on cooldown
                if (shootTimer == 0) {
                    reactionTimer++; // "I see the player! Aiming..."

                    if (reactionTimer >= REACTION_TIME) {
                        int bDx = facingLeft ? -6 : 6;
                        int spawnX = facingLeft ? x - 12 : x + width;
                        enemyBullets.add(new EnemyBullet(spawnX, y + (height / 2), bDx));

                        shootTimer = 120; // 2 seconds cooldown before they can shoot again
                        reactionTimer = 0; // Reset reaction
                    }
                }
            } else {
                reactionTimer = 0; // Player got behind them, reset reaction
            }
        } else {
            reactionTimer = 0; // Player is on a different floor, reset reaction
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void draw(Graphics2D g) {
        if (sprite != null) {
            if (speed > 0) g.drawImage(sprite, x, y, width, height, null);
            else g.drawImage(sprite, x + width, y, -width, height, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);

            // If the enemy has spotted you and is reacting, turn their eye yellow as a warning!
            if (reactionTimer > 0) g.setColor(Color.YELLOW);
            else g.setColor(Color.WHITE);

            if (speed > 0) g.fillRect(x + 30, y + 10, 10, 10);
            else g.fillRect(x + 8, y + 10, 10, 10);
        }
    }
}