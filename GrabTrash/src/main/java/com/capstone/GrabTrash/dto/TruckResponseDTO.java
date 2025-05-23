package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

/**
 * Data Transfer Object for truck responses
 * This class is used to send truck information back to the admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckResponseDTO {
    private String truckId;
    private String size;
    private String wasteType;
    private String status; // AVAILABLE, BUSY, MAINTENANCE, etc.
    private Date createdAt;
    private Date updatedAt;
    private String message;
} 