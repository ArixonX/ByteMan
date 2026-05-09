package com.byteman;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class ImageLoader {
    public static BufferedImage load(String path) {
        try {
            URL resource = ImageLoader.class.getResource(path);
            if (resource != null) return ImageIO.read(resource);
            System.out.println("Could not find image: " + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}