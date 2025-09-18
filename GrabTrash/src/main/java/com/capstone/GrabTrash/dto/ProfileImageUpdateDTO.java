package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for profile image upload
 * Used by all user roles to update their profile image
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileImageUpdateDTO {
    private String imageUrl;  // URL or base64 encoded image data
    private String imageType; // Type of image (e.g., "jpeg", "png")
}