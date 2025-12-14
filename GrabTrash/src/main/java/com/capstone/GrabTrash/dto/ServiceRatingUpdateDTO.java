package com.capstone.GrabTrash.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for service rating updates
 * This class is used to receive service rating information from customers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRatingUpdateDTO {
    private Integer serviceRating;  // Customer service rating (1-5 stars)
}