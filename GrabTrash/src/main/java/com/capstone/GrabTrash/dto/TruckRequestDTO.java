package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for truck requests
 * This class is used to receive truck information from admin
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckRequestDTO {
    private String size;
    private String wasteType;
    private String status; // AVAILABLE, BUSY, MAINTENANCE, etc.
} 