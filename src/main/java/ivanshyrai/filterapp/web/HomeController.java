package ivanshyrai.filterapp.web;

import ivanshyrai.filterapp.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
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
import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class HomeController {
    private final String UPLOADED_FOLDER = "/tmp/";
    private Path originPath;
    private Path convertedPath;
    private ImageService imageService;

    @Autowired
    public HomeController(ImageService imageService) {
        this.imageService = imageService;
    }

    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("uploadedImage", originPath);
        model.addAttribute("convertedImage", convertedPath);
        return "homePage";
    }

    @CacheEvict(value = "uploaded", allEntries = true)
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String singleFileUpload(@RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) throws MultipartException {
        if (file.isEmpty() || !isImage(file)) {
            redirectAttributes.addFlashAttribute("message",
                    "Wrong type of file");
            return "redirect:/";
        }
        if (file.getSize() >= 5 * 1024 * 1024) {
            redirectAttributes.addFlashAttribute("message",
                    "File must be less than 5MB");
            return "redirect:/";
        }
        try {
            convertedPath = null;
            // Get the file and save it
            byte[] bytes = file.getBytes();
            originPath = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(originPath, bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "redirect:/";
    }

    @Cacheable("uploaded")
    @RequestMapping(value = "/uploaded")
    public void getUploadedPicture(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", URLConnection.guessContentTypeFromName(originPath.toString()));
        Files.copy(originPath, response.getOutputStream());
    }

    @Cacheable("processed")
    @RequestMapping(value = "/converted")
    public void getConvertedImage(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Type", URLConnection.guessContentTypeFromName(convertedPath.toString()));
        Files.copy(convertedPath, response.getOutputStream());
    }

    @RequestMapping(value = "/convert", method = RequestMethod.POST)
    public String convert(@RequestParam String option) {
        File input = originPath.toFile();
        switch (option) {
            case "grayscale":
                convertedPath = imageService.convertToGrayScale(input);
                break;
            case "binary":
                convertedPath = imageService.convertToBinary(input);
                break;
            case "faceDetect":
                convertedPath = imageService.faceDetect(input);
                break;
            case "linearBlur":
                convertedPath = imageService.linearBlur(input);
                break;
            case "nonlinearMedian":
                convertedPath = imageService.nonlinearMedian(input);
                break;
        }
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
