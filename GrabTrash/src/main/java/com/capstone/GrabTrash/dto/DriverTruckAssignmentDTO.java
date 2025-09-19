package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for driver-truck assignment requests
 * This class is used to assign or remove drivers from trucks
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverTruckAssignmentDTO {
    private String truckId;
    private String driverId;
}