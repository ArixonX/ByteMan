package com.byteman;

import java.io.Serializable;

public class SaveState implements Serializable {
    private static final long serialVersionUID = 1L;
    public int level, lives, ammo, score;

    public SaveState(int level, int lives, int ammo, int score) {
        this.level = level; this.lives = lives;
        this.ammo = ammo; this.score = score;
    }
}