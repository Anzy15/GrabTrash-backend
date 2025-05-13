package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for dashboard statistics
 * This class is used to send summarized data for the dashboard
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private int totalPickupTrashOrdered;
    private double totalRevenue;
    private int totalCompletedOrders;
    private String message;
} 