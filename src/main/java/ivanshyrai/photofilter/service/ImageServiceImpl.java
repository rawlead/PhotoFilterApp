package ivanshyrai.photofilter.service;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageServiceImpl implements ImageService {
    private BufferedImage image;
    private final String UPLOADED_FOLDER = "/tmp/";

    @Override
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
            return Paths.get(writeImageToFile(input, "grayscale").toString());

        } catch (Exception e) {
        }
        return null;
    }

    @Override
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
            return Paths.get(writeImageToFile(input, "binary").toString());
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public Path faceDetect(Path path) {
        File input = path.toFile();
        try {
            image = ImageIO.read(input);
            nu.pattern.OpenCV.loadLibrary();
            CascadeClassifier faceDetector = new CascadeClassifier(getClass().getClassLoader()
                    .getResource("haarcascade_frontalface_alt.xml").getPath());
            Mat matrix = Highgui.imread(path.toString());
            MatOfRect faceDetetions = new MatOfRect();
            faceDetector.detectMultiScale(matrix, faceDetetions);

            for (Rect rect : faceDetetions.toArray()) {
                Core.rectangle(matrix, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 0));
            }

            image = convertMatToImage(matrix, input);
            return Paths.get(writeImageToFile(input, "face-detected").toString());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private File writeImageToFile(File input, String convertedName) {
        File output = null;
        try {
            String fileExtension = getFileExtension(input.getName());
            String newName = getPictureName(input, convertedName);
            output = new File(UPLOADED_FOLDER + newName + "." + fileExtension);

            ImageIO.write(image, fileExtension, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }


    public BufferedImage convertMatToImage(Mat matrix, File input) {
        //convert the matrix into a matrix of bytes appropriate for
        //this file extension
        MatOfByte mob = new MatOfByte();
        Highgui.imencode("." + getFileExtension(input.getName()), matrix, mob);
        //convert the "matrix of bytes" into a byte array
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;
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
