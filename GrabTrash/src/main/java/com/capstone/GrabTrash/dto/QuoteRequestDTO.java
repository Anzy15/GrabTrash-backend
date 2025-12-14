package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for quote/estimation requests
 * This class is used to receive information for price estimation and truck assignment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteRequestDTO {
    private String customerEmail;
    private String address;
    private Double latitude;
    private Double longitude;
    private String barangayId;
    private String phoneNumber;   // Customer phone number
    private String wasteType;     // Type of waste (e.g., RECYCLABLE, NON_RECYCLABLE, HAZARDOUS)
    private Double trashWeight;   // Weight of trash in kilograms
    private String notes;         // Additional notes or special instructions
}