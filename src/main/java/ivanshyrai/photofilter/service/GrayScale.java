package ivanshyrai.photofilter.service;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Component
public class GrayScale {
    private static BufferedImage image;

    public static void convert(File input) {
        String name = input.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        String newName = name + "-grayscale";

        try {

            image = ImageIO.read(input);

            int width = image.getWidth();
            int height = image.getHeight();

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    Color c = new Color(image.getRGB(j, i));
                    int red = (int) (c.getRed() * 0.299);
                    int green = (int) (c.getGreen() * 0.587);
                    int blue = (int) (c.getBlue() * 0.114);

                    Color newColor = new Color(
                            red + green + blue,
                            red + green + blue,
                            red + green + blue);
                    image.setRGB(j, i, newColor.getRGB());
                }
            }

            String fileExtension = getFileExtension(input.getName());
            File output = new File("/tmp/" + newName + "." + fileExtension);
            ImageIO.write(image, fileExtension, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileExtension(String name) {
        return name.substring(name.lastIndexOf(".") + 1);
    }
}
