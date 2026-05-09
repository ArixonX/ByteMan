package com.byteman;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Player {
    private int x, y;
    private int width = 48, height = 64;
    private double dy = 0;
    private boolean facingRight = true, isJumping = true;
    private boolean leftPressed = false, rightPressed = false;

    public int lives = 3, maxAmmo = 10, currentAmmo = maxAmmo, score = 0;
    private int reloadTimer = 0;
    private static final int RELOAD_TIME = 60;
    private static final double JUMP_STRENGTH = -15.0;
    private static final int MOVE_SPEED = 6;
    private BufferedImage sprite;

    public Player(int startX, int startY) {
        this.x = startX; this.y = startY;
        this.sprite = ImageLoader.load("/player.png");
    }

    public void setLeftPressed(boolean b) { leftPressed = b; if(b) facingRight = false; }
    public void setRightPressed(boolean b) { rightPressed = b; if(b) facingRight = true; }
    public void jump() { if (!isJumping) { dy = JUMP_STRENGTH; isJumping = true; } }

    public void shoot(ArrayList<Bullet> bullets, boolean hasRapidFire) {
        if (hasRapidFire || (currentAmmo > 0 && reloadTimer == 0)) {
            int bulletDX = facingRight ? 12 : -12;
            int startX = facingRight ? x + width + 4 : x - 12;
            bullets.add(new Bullet(startX, y + (height / 2), bulletDX));

            if (!hasRapidFire) {
                currentAmmo--;
                if (currentAmmo == 0) reloadTimer = RELOAD_TIME;
            }
        }
    }

    public void update(ArrayList<Platform> platforms) {
        if (reloadTimer > 0) {
            reloadTimer--;
            if (reloadTimer == 0) currentAmmo = maxAmmo;
        }

        int dx = 0;
        if (leftPressed) dx -= MOVE_SPEED;
        if (rightPressed) dx += MOVE_SPEED;
        x += dx;
        if (x < 0) x = 0;

        dy += GameConstants.GRAVITY;
        y += dy;
        isJumping = true;

        Rectangle playerFeet = new Rectangle(x, y, width, height + 1);

        for (Platform plat : platforms) {
            Rectangle pRect = plat.getBounds();
            if (dy >= 0 && playerFeet.intersects(pRect) && y + height - (int)dy <= pRect.y) {
                y = pRect.y - height;
                dy = 0;
                isJumping = false;
            }
        }

        if (y + height >= GameConstants.FLOOR_Y) {
            y = GameConstants.FLOOR_Y - height;
            dy = 0;
            isJumping = false;
        }
    }

    public void resetPosition(int startX, int startY) { this.x = startX; this.y = startY; this.dy = 0; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public int getX() { return x; }
    // THE MISSING METHOD IS ADDED HERE:
    public int getY() { return y; }

    public void draw(Graphics2D g) {
        if (sprite != null) {
            if (facingRight) g.drawImage(sprite, x, y, width, height, null);
            else g.drawImage(sprite, x + width, y, -width, height, null);
        } else {
            g.setColor(Color.BLUE); g.fillRect(x, y, width, height);
        }
    }
}