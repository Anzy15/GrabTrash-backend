package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for driver assignment requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverAssignmentDTO {
    private String paymentId;
    private String driverId;
} 