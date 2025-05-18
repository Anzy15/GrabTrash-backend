package com.capstone.GrabTrash.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class for storing payment information
 * This class represents a payment record in the system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private String id;
    private String orderId;
    private String customerName;
    private String customerEmail;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double amount;
    private Double tax;
    private Double totalAmount;
    private String paymentMethod;
    private String paymentReference;
    private String notes;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private String barangayId;
    
    // Pre-persist hook to set dates
    public void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        updatedAt = new Date();
    }
}