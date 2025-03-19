package com.corefit.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.corefit.exceptions.GeneralException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FilesService {

    private static final Logger logger = LoggerFactory.getLogger(FilesService.class);
    private final Cloudinary cloudinary;

    @Value("${cloudinary.base-location}")
    private String basePathLocation;

    public FilesService(
            @Value("${cloudinary.cloud-name}") String cloudName,
            @Value("${cloudinary.api-key}") String apiKey,
            @Value("${cloudinary.api-secret}") String apiSecret) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret));
    }

    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null.");
        }

        try {
            // ✅ Fixed basePathLocation reference
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", basePathLocation, // Organize images in a folder
                    "resource_type", "image"     // Ensure only image uploads
            ));

            return uploadResult.get("secure_url").toString(); // Return Cloudinary URL

        } catch (IOException e) {
            logger.error("Error uploading file to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload image to Cloudinary.", e);
        } catch (Exception e) {
            logger.error("Unexpected error during image upload: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during image upload.", e);
        }
    }

    public void deleteImage(String imageUrl) throws IOException {
        String publicId = extractPublicId(imageUrl);
        if (publicId == null || publicId.trim().isEmpty()) {
            throw new IllegalArgumentException("Public ID cannot be null or empty.");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));

            String deletionStatus = (String) result.get("result");
            if ("not_found".equals(deletionStatus)) {
                throw new IOException("Image not found on Cloudinary: " + publicId);
            }

        } catch (IOException e) {
            throw new IOException("Failed to delete image from Cloudinary.", e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during image deletion.", e);
        }
    }

    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL cannot be null or empty.");
        }

        try {
            int uploadIndex = imageUrl.indexOf("/upload/");
            if (uploadIndex == -1) {
                throw new IllegalArgumentException("Invalid Cloudinary image URL.");
            }

            uploadIndex += 8; // Move to the actual file path after "/upload/"
            String pathAfterUpload = imageUrl.substring(uploadIndex);

            if (pathAfterUpload.contains("?")) {
                pathAfterUpload = pathAfterUpload.substring(0, pathAfterUpload.indexOf("?"));
            }

            int extensionIndex = pathAfterUpload.lastIndexOf(".");
            if (extensionIndex != -1) {
                pathAfterUpload = pathAfterUpload.substring(0, extensionIndex);
            }

            return pathAfterUpload;

        } catch (Exception e) {
            throw new IllegalArgumentException("Error extracting publicId from URL: " + imageUrl, e);
        }
    }

    public List<String> uploadImages(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return new ArrayList<>();
        }
        return images.stream().map(image -> {
            try {
                return saveImage(image);
            } catch (IOException e) {
                throw new GeneralException("Failed to upload image: " + e.getMessage());
            }
        }).collect(Collectors.toList());
    }

    public void deleteImages(List<String> images) {
        if (images != null && !images.isEmpty()) {
            images.forEach(image -> {
                try {
                    deleteImage(image);
                } catch (IOException e) {
                    throw new GeneralException("Failed to delete image: " + e.getMessage());
                }
            });
        }
    }
}
