package com.corefit.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class FilesService {
    private static final Logger logger = LoggerFactory.getLogger(FilesService.class);
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + File.separator + "uploads";
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/svg+xml");

    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            logger.error("Uploaded file is null or empty");
            throw new IOException("File is empty or null.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            logger.error("Unsupported file type: {}", contentType);
            throw new IOException("Invalid file type. Only JPG, JPEG, PNG, and SVG are allowed.");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            logger.error("Uploaded file has no valid name");
            throw new IOException("File name is invalid.");
        }

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            logger.info("Upload directory created: {}", UPLOAD_DIR);
        }

        String fileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Path filePath = uploadPath.resolve(fileName);

        file.transferTo(filePath.toFile());
        logger.info("File saved successfully: {}", filePath.toString());

        return  "http://localhost:8000/uploads/" + fileName;
    }

    public void deleteImage(String imagePath) throws IOException {
        if (imagePath.startsWith("http://localhost:8000/uploads/")) {
            imagePath = imagePath.replace("http://localhost:8000/uploads/", "");
        }

        Path path = Paths.get("uploads").resolve(imagePath).normalize();
        Files.deleteIfExists(path);
    }
}
