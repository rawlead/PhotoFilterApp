package ivanshyrai.photofilter.service;

import java.nio.file.Path;

public interface ImageService {

    Path convertToGrayScale(Path path);

    Path convertToBinary(Path path);

    Path faceDetect(Path path);
}
