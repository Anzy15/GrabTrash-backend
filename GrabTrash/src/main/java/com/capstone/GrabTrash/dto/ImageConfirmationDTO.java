package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for image confirmation upload
 * Used by both customers and drivers to upload proof images
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageConfirmationDTO {
    private String imageUrl;  // URL or base64 encoded image data
    private String imageType; // Type of image (e.g., "jpeg", "png")
    private String description; // Optional description of the image
}