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
    private String driverId;
    private double amount;
    private double tax;
    private double totalAmount;
}