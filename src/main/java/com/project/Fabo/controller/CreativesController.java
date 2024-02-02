package com.project.Fabo.controller;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import com.project.Fabo.repository.ImageRepository;

@Controller
public class CreativesController {

    @Autowired
    private ImageRepository imageRepository;

    // Endpoint to display image upload form
    @GetMapping("/upload")
    public String showUploadForm() {
        return "creativesU"; // Assuming you have a "upload.html" view
    }
    
    @GetMapping("/test")
    public String show() {
        return "loginPage"; // Assuming you have a "upload.html" view
    }
    
   /* 
 // Endpoint to handle image upload
    @PostMapping("/upload")
    public String handleImageUpload(@RequestParam("file") MultipartFile file) {
        try {
            ImageEntity imageEntity = new ImageEntity();
            imageEntity.setImageData(file.getBytes());
            imageRepository.save(imageEntity);
        } catch (IOException e) {
            // Handle exception
        }
        return "redirect:/upload";
    }
    
 // Endpoint to display multiple images
    @GetMapping("/displayImages")
    public String displayImages(Model model) {
        // Assuming you have a list of image IDs or any other criteria
        List<Long> imageIds = Arrays.asList(1L, 2L, 3L); // Example image IDs
        
        List<ImageEntity> images = new ArrayList<>();
        for (Long id : imageIds) {
            Optional<ImageEntity> imageOptional = imageRepository.findById(id);
            imageOptional.ifPresent(images::add);
        }

        model.addAttribute("images", images);
        return "displayImages"; // Assuming you have a "displayImages.html" view
    }
    */
}