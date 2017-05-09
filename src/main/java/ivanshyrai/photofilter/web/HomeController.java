package ivanshyrai.photofilter.web;

import ivanshyrai.photofilter.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class HomeController {
    private final String UPLOADED_FOLDER = "/tmp/";
    private Path path;
    private Path convertedPath;
    private ImageService imageService;

    @Autowired
    public HomeController(ImageService imageService) {
        this.imageService = imageService;
    }

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("uploadedImage",path);
        model.addAttribute("convertedImage",convertedPath);
        return "homePage";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) throws MultipartException{
        if (file.isEmpty() || !isImage(file)) {
            redirectAttributes.addFlashAttribute("message",
                    "Wrong type of file");
            return "redirect:/";
        }
        if (file.getSize() >= 5 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("message",
                    "File size must be less than 5MB");
            return "redirect:/";
        }
        try {
            // Get the file and save it
            byte[] bytes = file.getBytes();
            path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/uploaded")
    public void getUploadedPicture(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", URLConnection.guessContentTypeFromName(path.toString()));
        Files.copy(path, response.getOutputStream());
    }
    @RequestMapping(value = "/converted")
    public void getConvertedImage(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", URLConnection.guessContentTypeFromName(convertedPath.toString()));
        Files.copy(convertedPath, response.getOutputStream());
    }

    @RequestMapping("/grayscale")
    public String convertImage() {
        convertedPath = imageService.convertToGrayScale(path);
        return "redirect:/";
    }

    @RequestMapping("/binary")
    public String binaryImage() {
        convertedPath = imageService.convertToBinary(path);
        return "redirect:/";
    }

    @RequestMapping("/facedetect")
    public String faceDetect() {
        convertedPath = imageService.faceDetect(path);
        return "redirect:/";
    }

    private boolean isImage(MultipartFile file) {
        return file.getContentType().startsWith("image");
    }

    @ExceptionHandler(IllegalStateException.class)
    public String fileSizeTooBig() {
        return "redirect:/";
    }
}
