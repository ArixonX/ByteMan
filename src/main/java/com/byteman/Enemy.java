package com.byteman;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Enemy {
    private int x, y, startX, patrolRange, speed;
    // INCREASED SIZE:
    private int width = 50, height = 50;
    private BufferedImage sprite;

    public Enemy(int x, int y, int patrolRange, int speed) {
        this.x = x; this.y = y; this.startX = x;
        this.patrolRange = patrolRange; this.speed = speed;
        this.sprite = ImageLoader.load("/enemy.png");
    }

    public void update() {
        if (patrolRange > 0) {
            x += speed;
            if (x > startX + patrolRange || x < startX - patrolRange) speed = -speed;
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public void draw(Graphics2D g) {
        if (sprite != null) g.drawImage(sprite, x, y, width, height, null);
        else { g.setColor(Color.RED); g.fillOval(x, y, width, height); }
    }
}