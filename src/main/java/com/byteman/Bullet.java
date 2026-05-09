package com.byteman;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Bullet {
    private int x, y, dx;
    // INCREASED SIZE:
    private int size = 16;
    private BufferedImage sprite;

    public Bullet(int x, int y, int dx) {
        this.x = x; this.y = y; this.dx = dx;
        this.sprite = ImageLoader.load("/bullet.png");
    }

    public void update() { x += dx; }
    public Rectangle getBounds() { return new Rectangle(x, y, size, size); }
    public boolean isOffScreen() { return x < 0 || x > GameConstants.WIDTH; }

    public void draw(Graphics2D g) {
        if (sprite != null) g.drawImage(sprite, x, y, size, size, null);
        else { g.setColor(Color.YELLOW); g.fillOval(x, y, size, size); }
    }
}