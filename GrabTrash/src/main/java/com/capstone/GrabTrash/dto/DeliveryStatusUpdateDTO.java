package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for updating delivery status
 * This class is used to update the isDelivered field of a payment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryStatusUpdateDTO {
    private Boolean isDelivered;
} 