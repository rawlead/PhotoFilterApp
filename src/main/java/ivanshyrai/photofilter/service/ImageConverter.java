package ivanshyrai.photofilter.service;

import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ImageConverter {
    private BufferedImage image;
    private final String UPLOADED_FOLDER = "/tmp/";

    public Path convertToGrayScale(Path path) {
        File input = path.toFile();
        try {
            image = ImageIO.read(input);
            int width = image.getWidth();
            int height = image.getHeight();
            int red, green, blue;
            Color color;

            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    color = new Color(image.getRGB(j, i));
                    red = (int) (color.getRed() * 0.299);
                    green = (int) (color.getGreen() * 0.587);
                    blue = (int) (color.getBlue() * 0.114);
                    image.setRGB(j, i, new Color(
                            red + green + blue,
                            red + green + blue,
                            red + green + blue)
                            .getRGB());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Paths.get(writeImageToFile(input,"grayscale").toString());
    }

    public Path convertToBinary(Path path) {
        File input = path.toFile();
        try {
            image = ImageIO.read(input);
            int width = image.getWidth();
            int height = image.getHeight();
            Color color;

            int basicColor;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    color = new Color(image.getRGB(j, i));
                    basicColor = (color.getRed() < 128 ||
                            color.getGreen() < 128 ||
                            color.getBlue() < 128) ? 0 : 255;
                    image.setRGB(j, i, new Color(
                            basicColor, basicColor, basicColor)
                            .getRGB());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Paths.get(writeImageToFile(input,"binary").toString());
    }

    private File writeImageToFile(File input,String convertedName) {
        String fileExtension = getFileExtension(input.getName());
        String newName = getPictureName(input,convertedName);
        File output = new File(UPLOADED_FOLDER + newName + "." + fileExtension);
        try {
            ImageIO.write(image,fileExtension,output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }

    private String getPictureName(File input, String convertedName) {
        String name = input.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        return name + "-" + convertedName;
    }

    private static String getFileExtension(String name) {
        return name.substring(name.lastIndexOf(".") + 1);
    }
}
