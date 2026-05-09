package com.byteman;

import java.awt.*;
import java.awt.image.BufferedImage;

public class PowerUp {
    // THIS IS THE LINE JAVA IS LOOKING FOR:
    public enum Type { RAPID_FIRE, SLOW_MOTION }

    private int x, y;
    private int size = 32;
    private Type type;
    private BufferedImage sprite;

    public PowerUp(int x, int y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;

        if (type == Type.RAPID_FIRE) {
            this.sprite = ImageLoader.load("/goldcube.png");
        } else {
            this.sprite = ImageLoader.load("/cyancube.png");
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, size, size); }
    public Type getType() { return type; }

    public void draw(Graphics2D g) {
        if (sprite != null) {
            g.drawImage(sprite, x, y, size, size, null);
        } else {
            if (type == Type.RAPID_FIRE) {
                g.setColor(Color.ORANGE);
                g.fillRect(x, y, size, size);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString("R", x + 8, y + 22);
            } else {
                g.setColor(Color.CYAN);
                g.fillRect(x, y, size, size);
                g.setColor(Color.BLACK);
                g.setFont(new Font("Arial", Font.BOLD, 16));
                g.drawString("S", x + 8, y + 22);
            }
        }
    }
}