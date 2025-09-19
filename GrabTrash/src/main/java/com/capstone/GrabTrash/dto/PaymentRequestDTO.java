package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for payment requests
 * This class is used to receive payment information from the mobile app
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDTO {
    private String orderId;
    private String customerName;
    private String customerEmail;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double amount;
    private Double totalAmount;
    private String paymentMethod;
    private String paymentReference;
    private String notes;
    private String barangayId;
    private String wasteType;  // Type of waste (e.g., RECYCLABLE, NON_RECYCLABLE, HAZARDOUS)
    private Double trashWeight; // Weight of trash in kilograms
    private String truckId;    // ID of the selected truck
}