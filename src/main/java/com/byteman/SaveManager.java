package com.byteman;

import java.io.*;

public class SaveManager {
    private static final String SAVE_FILE = "byteman_save.dat";

    public static void save(SaveState state) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            oos.writeObject(state);
            System.out.println("Game saved!");
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static SaveState load() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            return (SaveState) ois.readObject();
        } catch (IOException | ClassNotFoundException e) { return null; }
    }
}