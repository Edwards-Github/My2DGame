package main;

import java.awt.*;
import java.awt.image.BufferedImage;

public class UtilityTool {

    public BufferedImage scaleImage(BufferedImage original, int width, int height) {
        // Make an already scaled image to improve rendering image time
        BufferedImage scaledImage = new BufferedImage(width, height, original.getType());
        Graphics2D g2 = scaledImage.createGraphics(); // save image into scaledImage
        g2.drawImage(original, 0, 0, width, height, null); // draw image
        g2.dispose();

        return scaledImage;
    }
}
