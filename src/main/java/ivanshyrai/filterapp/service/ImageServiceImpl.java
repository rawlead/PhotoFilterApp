package ivanshyrai.filterapp.service;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.highgui.Highgui;
import org.opencv.objdetect.CascadeClassifier;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

@Service
public class ImageServiceImpl implements ImageService {
    private BufferedImage image;
    private final String UPLOADED_FOLDER = "/tmp/";

    @Override
    public Path convertToGrayScale(File input) {
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
            File output = writeImageToFile(input, "grayscale");
            return Paths.get(output.toString());

        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public Path convertToBinary(File input) {
        try {
            image = ImageIO.read(input);
            int width = image.getWidth();
            int height = image.getHeight();
            Color originPixel;

            int binaryPixel;
            for (int i = 0; i < height; i++) {
                for (int j = 0; j < width; j++) {
                    originPixel = new Color(image.getRGB(j, i));
                    binaryPixel = (originPixel.getRed() < 128 ||
                            originPixel.getGreen() < 128 ||
                            originPixel.getBlue() < 128) ? 0 : 255;
                    image.setRGB(j, i, new Color(
                            binaryPixel, binaryPixel, binaryPixel)
                            .getRGB());
                }
            }
            File output = writeImageToFile(input, "binary");
            return Paths.get(output.toString());

        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public Path nonlinearMedian(File input) {
        try {

            image = ImageIO.read(input);
            int width = image.getWidth();
            int height = image.getHeight();

            Color[] pixel = new Color[9];
            int[] R = new int[9];
            int[] G = new int[9];
            int[] B = new int[9];

            for (int i = 1; i < width - 1; i++) {
                for (int j = 1; j < height - 1; j++) {
//                    get every surrounded pixels 9x9 matrix
                    pixel[0] = new Color(image.getRGB(i - 1, j - 1));
                    pixel[1] = new Color(image.getRGB(i - 1, j));
                    pixel[2] = new Color(image.getRGB(i - 1, j + 1));
                    pixel[3] = new Color(image.getRGB(i, j + 1));
                    pixel[4] = new Color(image.getRGB(i + 1, j + 1));
                    pixel[5] = new Color(image.getRGB(i + 1, j));
                    pixel[6] = new Color(image.getRGB(i + 1, j - 1));
                    pixel[7] = new Color(image.getRGB(i, j - 1));
                    pixel[8] = new Color(image.getRGB(i, j));

//                    get each red,green and blue value
                    for (int k = 0; k < pixel.length; k++) {
                        R[k] = pixel[k].getRed();
                        G[k] = pixel[k].getGreen();
                        B[k] = pixel[k].getBlue();
                    }

//                    sort each color of a pixel and get middle value - assign to current pixel after
                    Arrays.sort(R);
                    Arrays.sort(G);
                    Arrays.sort(B);
                    image.setRGB(i, j, new Color(R[4], G[4], B[4]).getRGB());
                }
            }
            File output = writeImageToFile(input, "median");
            return Paths.get(output.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }


        return null;
    }

    @Override
    public Path linearBlur(File input) {
        int filterWidth = 12;
        int filterHeight = 12;
//        kernel 1/6 1/6 1/6
//        float[] filterMatrix = {
//                0.111f, 0.111f, 0.111f,
//                0.111f, 0.111f, 0.111f,
//                0.111f, 0.111f, 0.111f,
//        };

        float[] filterMatrix = new float[144];
        for (int i = 0; i < 144; i++)
            filterMatrix[i] = 1.0f / 144.0f;

        try {
            image = ImageIO.read(input);
            BufferedImageOp op = new ConvolveOp(new Kernel(filterWidth, filterHeight, filterMatrix),
                    ConvolveOp.EDGE_ZERO_FILL,null);
            image = op.filter(image,
                    new BufferedImage(image.getWidth(),
                            image.getHeight(),
                            image.getType()));
            File output = writeImageToFile(input, "blurred");
            return Paths.get(output.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public Path faceDetect(File input) {
        try {
            image = ImageIO.read(input);
            nu.pattern.OpenCV.loadLibrary();
//             xml file with face detect
            CascadeClassifier faceDetector = new CascadeClassifier(getClass().getClassLoader()
                    .getResource("haarcascade_frontalface_alt.xml").getPath());
            Mat matrix = Highgui.imread(input.toPath().toString());
            MatOfRect faceDetetions = new MatOfRect();
            faceDetector.detectMultiScale(matrix, faceDetetions);

            for (Rect rect : faceDetetions.toArray()) {
                Core.rectangle(
                        matrix,
                        new Point(rect.x, rect.y),
                        new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 0));
            }

            image = convertMatToImage(matrix, input);
            File output = writeImageToFile(input, "face");
            return Paths.get(output.toString());

        } catch (Exception e) {
        }
        return null;
    }


    private File writeImageToFile(File input, String endingName) {
        File output = null;
        try {
            String fileExtension = getFileExtension(input.getName());
            String newName = getPictureName(input, endingName);
            output = new File(UPLOADED_FOLDER + newName + "." + fileExtension);

            ImageIO.write(image, fileExtension, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return output;
    }


    private BufferedImage convertMatToImage(Mat matrix, File input) {
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


    private String getPictureName(File input, String additionName) {
        String name = input.getName();
        name = name.substring(0, name.lastIndexOf('.'));
        return name + "-" + additionName;
    }

    private static String getFileExtension(String name) {
        return name.substring(name.lastIndexOf(".") + 1);
    }
}
