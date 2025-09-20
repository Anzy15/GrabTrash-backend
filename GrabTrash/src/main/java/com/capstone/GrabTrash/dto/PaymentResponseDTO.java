package com.capstone.GrabTrash.dto;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for payment responses
 * This class is used to send payment confirmation back to the mobile app
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {
    private String id;
    private String orderId;
    private String status;
    private String paymentMethod;
    private String paymentReference;
    private Date createdAt;
    private String message;
    private String barangayId;
    private String address;
    private double latitude;
    private double longitude;
    private String phoneNumber;
    private String customerName;
    private String customerEmail;
    private String driverId;
    private double amount;
    private double totalAmount;
    private String notes;          // Customer notes or special instructions
    private String wasteType;  // Type of waste (e.g., RECYCLABLE, NON_RECYCLABLE, HAZARDOUS)
    private Double trashWeight; // Weight of trash in kilograms
    private String truckId;    // ID of the assigned truck
    private String jobOrderStatus;  // Status of the job order (NEW, IN_PROGRESS, COMPLETED, CANCELLED)
    private Boolean isDelivered;  // Whether the waste has been delivered to the disposal facility
    private String customerConfirmation;  // Customer confirmation proof image URL
    private String driverConfirmation;  // Driver confirmation proof image URL
}