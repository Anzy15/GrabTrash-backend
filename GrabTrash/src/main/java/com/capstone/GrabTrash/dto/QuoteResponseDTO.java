package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for quote/estimation responses
 * This class contains the estimated pricing and assigned truck/driver information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponseDTO {
    private String quoteId;           // Unique quote ID for reference
    private Double estimatedAmount;   // Base estimated amount
    private Double estimatedTotalAmount; // Total estimated amount
    private String assignedTruckId;   // ID of the assigned truck
    private String assignedDriverId;  // ID of the assigned driver
    private String truckDetails;      // Additional truck information
    private String driverDetails;     // Additional driver information
    private Double truckCapacity;     // Capacity of the assigned truck
    private String message;           // Response message
    private Boolean automationSuccess; // Whether automation was successful
    private String wasteType;         // Confirmed waste type
    private Double trashWeight;       // Confirmed trash weight
    private String phoneNumber;       // Customer phone number
    private String notes;             // Customer notes or special instructions
}