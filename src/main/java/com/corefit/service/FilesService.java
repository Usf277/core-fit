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
                "api_secret", apiSecret));
    }

    /**
     * Uploads an image to Cloudinary.
     *
     * @param file MultipartFile to upload
     * @return URL of the uploaded image
     * @throws IOException If upload fails or the file is invalid
     */
    public String saveImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null.");
        }

        try {
            // Upload file to Cloudinary
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", "corefit/uploads", // Organize images in a folder
                    "resource_type", "image"     // Ensure only image uploads
            ));

            String imageUrl = uploadResult.get("secure_url").toString();

            return imageUrl; // Return the Cloudinary URL

        } catch (IOException e) {
            logger.error("Error uploading file to Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to upload image to Cloudinary.", e);
        } catch (Exception e) {
            logger.error("Unexpected error during image upload: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during image upload.", e);
        }
    }

    /**
     * Deletes an image from Cloudinary by its public ID.
     *
     * @throws IOException If deletion fails
     */
    public void deleteImage(String imageUrl) throws IOException {
        String publicId = extractPublicId(imageUrl);
        if (publicId == null || publicId.trim().isEmpty()) {
            logger.error("Public ID for image deletion is null or empty.");
            throw new IllegalArgumentException("Public ID cannot be null or empty.");
        }

        try {
            logger.info("Deleting image with publicId: {}", publicId);

            // Delete image from Cloudinary
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                    "resource_type", "image"
            ));

            String deletionStatus = result.get("result").toString();
            if ("not_found".equals(deletionStatus)) {
                logger.warn("Image not found in Cloudinary: {}", publicId);
                throw new IOException("Image not found: " + publicId);
            }

            logger.info("Image deleted successfully: {}", publicId);

        } catch (IOException e) {
            logger.error("Error deleting image from Cloudinary: {}", e.getMessage(), e);
            throw new IOException("Failed to delete image from Cloudinary.", e);
        } catch (Exception e) {
            logger.error("Unexpected error during image deletion: {}", e.getMessage(), e);
            throw new RuntimeException("Unexpected error during image deletion.", e);
        }
    }

    /**
     * Extracts the publicId from a Cloudinary image URL.
     *
     * @param imageUrl The Cloudinary image URL
     * @return Extracted publicId
     * @throws IllegalArgumentException If the URL is invalid
     */
    public String extractPublicId(String imageUrl) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            logger.error("Image URL is null or empty.");
            throw new IllegalArgumentException("Image URL cannot be null or empty.");
        }

        try {
            // Extract publicId (between "/upload/" and the file extension)
            int uploadIndex = imageUrl.indexOf("/upload/") + 7;
            int extensionIndex = imageUrl.lastIndexOf(".");
            if (uploadIndex == -1 || extensionIndex == -1) {
                throw new IllegalArgumentException("Invalid Cloudinary image URL.");
            }

            String publicId = imageUrl.substring(uploadIndex, extensionIndex);
            logger.info("Extracted publicId from URL: {}", publicId);
            return publicId;

        } catch (Exception e) {
            logger.error("Error extracting publicId from URL: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Error extracting publicId from URL.", e);
        }
    }
}
