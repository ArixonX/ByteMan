package com.byteman;

import java.awt.*;

public class EnemyBullet {
    private int x, y, dx;
    private int size = 12; // Slightly smaller than player bullets

    public EnemyBullet(int x, int y, int dx) {
        this.x = x;
        this.y = y;
        this.dx = dx;
    }

    public void update() {
        x += dx;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, size, size);
    }

    public boolean isOffScreen() {
        return x < 0 || x > GameConstants.WIDTH;
    }

    public void draw(Graphics2D g) {
        g.setColor(Color.RED);
        g.fillOval(x, y, size, size);
        g.setColor(Color.ORANGE);
        g.drawOval(x, y, size, size);
    }
}