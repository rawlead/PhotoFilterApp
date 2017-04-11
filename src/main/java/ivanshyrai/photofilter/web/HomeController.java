package ivanshyrai.photofilter.web;

import ivanshyrai.photofilter.service.ImageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
    private static String UPLOADED_FOLDER = "/tmp/";
    private Path path;
    private Path convertedPath;
    private ImageConverter imageConverter;

    @Autowired
    public HomeController(ImageConverter imageConverter) {
        this.imageConverter = imageConverter;
    }

    @RequestMapping("/")
    public String home(Model model) {
        if (path != null)
            model.addAttribute("imageIsUploaded", true);
        if (convertedPath != null)
            model.addAttribute("imageIsConverted", true);
        return "homePage";
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {
        if (file.isEmpty() || !isImage(file)) {
            redirectAttributes.addFlashAttribute("message", "Wrong type of file. Should be a picture.");
            return "redirect:/";
        }
        try {
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);
            redirectAttributes.addFlashAttribute("message", "You successfully uploaded " + file.getOriginalFilename());
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

    @RequestMapping("/grayscale")
    public String convertImage() {
        convertedPath = imageConverter.convertToGrayScale(path);
        return "redirect:/";
    }

    @RequestMapping("/binary")
    public String binaryImage() {
        convertedPath = imageConverter.convertToBinary(path);
        return "redirect:/";
    }


    @RequestMapping(value = "/converted")
    public void getConvertedImage(HttpServletResponse response) throws IOException {

        response.setHeader("Content-Type", URLConnection.guessContentTypeFromName(convertedPath.toString()));
        Files.copy(convertedPath, response.getOutputStream());
    }

    private boolean isImage(MultipartFile file) {
        return file.getContentType().startsWith("image");
    }
}
