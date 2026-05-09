package com.byteman;

import java.awt.*;

public class Platform {
    private Rectangle rect;

    public Platform(int x, int y, int width, int height) {
        rect = new Rectangle(x, y, width, height);
    }

    public Rectangle getBounds() { return rect; }

    public void draw(Graphics2D g2d) {
        g2d.setColor(new Color(50, 50, 70));
        g2d.fillRect(rect.x, rect.y, rect.width, rect.height);
        g2d.setColor(Color.CYAN);
        g2d.fillRect(rect.x, rect.y, rect.width, 2);
    }
}