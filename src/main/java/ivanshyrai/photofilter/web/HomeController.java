package ivanshyrai.photofilter.web;

import ivanshyrai.photofilter.service.GrayScale;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
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

    private boolean isGrayScale = true;

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

    @RequestMapping("/convert")
    public String convertImage() {
        isGrayScale = true;
        GrayScale.convert(path.toFile(),isGrayScale);
        System.out.println("conversion is done");

        String image = path.toString();
        int extension = image.lastIndexOf('.');
        String convertedImage = image.substring(0, extension)
                + "-grayscale" + image.substring(extension, image.length());
        convertedPath = Paths.get(convertedImage);

        return "redirect:/";
    }

    @RequestMapping("/binary")
    public String binaryImage() {
        isGrayScale = false;
        GrayScale.convert(path.toFile(),isGrayScale);
        System.out.println("conversion is done");

        String image = path.toString();
        int extension = image.lastIndexOf('.');
        String convertedImage = image.substring(0, extension)
                + "-grayscale" + image.substring(extension, image.length());
        convertedPath = Paths.get(convertedImage);

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
