package com.example.ecoswap.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageStorageService {

    @Value("${file.upload-dir:uploads/products}")
    private String uploadDir;

    /**
     * Initialize the upload directory
     */
    public void init() {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directory!", e);
        }
    }

    /**
     * Store an uploaded image file
     * @param file The multipart file to store
     * @return The filename of the stored file
     */
    public String storeImage(MultipartFile file) {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Failed to store empty file");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Get original filename and extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);

        // Generate unique filename
        String newFilename = UUID.randomUUID().toString() + fileExtension;

        try {
            // Ensure upload directory exists
            init();

            // Copy file to upload directory
            Path destinationPath = Paths.get(uploadDir).resolve(newFilename);
            Files.copy(file.getInputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING);

            return newFilename;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + newFilename, e);
        }
    }

    /**
     * Delete an image file
     * @param filename The filename to delete
     */
    public void deleteImage(String filename) {
        if (filename == null || filename.isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log the error but don't throw exception
            System.err.println("Failed to delete file: " + filename);
        }
    }

    /**
     * Load an image file as a Path
     * @param filename The filename to load
     * @return Path to the file
     */
    public Path loadImage(String filename) {
        return Paths.get(uploadDir).resolve(filename);
    }

    /**
     * Get file extension from filename
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) {
            return "";
        }
        return filename.substring(lastDot);
    }

    /**
     * Validate image file size (max 5MB)
     */
    public void validateImageSize(MultipartFile file) {
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size must not exceed 5MB");
        }
    }
}
