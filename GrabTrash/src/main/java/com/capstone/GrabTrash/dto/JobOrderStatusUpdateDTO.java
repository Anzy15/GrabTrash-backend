package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating job order status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobOrderStatusUpdateDTO {
    private String jobOrderStatus; // NEW, IN_PROGRESS, COMPLETED, CANCELLED
} 