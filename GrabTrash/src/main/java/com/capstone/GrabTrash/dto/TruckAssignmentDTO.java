package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for truck assignment
 * This class is used to assign a truck to a payment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TruckAssignmentDTO {
    private String paymentId;
    private String truckId;
} 