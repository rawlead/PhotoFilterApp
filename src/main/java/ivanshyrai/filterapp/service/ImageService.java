package ivanshyrai.filterapp.service;

import java.io.File;
import java.nio.file.Path;

public interface ImageService {

    Path convertToGrayScale(File file);

    Path convertToBinary(File file);

    Path faceDetect(File file);

    Path linearBlur(File file);

    Path nonlinearMedian(File input);
}
