package com.corefit.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FilesService {

    private static final Logger logger = LoggerFactory.getLogger(FilesService.class);

    private final Cloudinary cloudinary;

    public FilesService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            logger.error("Uploaded file is null or empty");
            throw new IOException("File is empty or null.");
        }

        logger.info("Uploading file: {}", file.getOriginalFilename());

        // Upload file to Cloudinary
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "corefit/uploads", // Organize images in a folder
                "resource_type", "image"     // Ensure only image uploads
        ));

        String imageUrl = uploadResult.get("secure_url").toString();
        logger.info("File uploaded to Cloudinary: {}", imageUrl);

        return imageUrl; // Return the Cloudinary URL
    }

    public void deleteImage(String publicId) throws IOException {
        logger.info("Deleting image with publicId: {}", publicId);

        Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                "resource_type", "image"
        ));

        logger.info("Deletion result: {}", result);
    }
}
