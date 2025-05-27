package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.dto.JobOrderStatusUpdateDTO;
import com.capstone.GrabTrash.dto.PaymentResponseDTO;
import com.capstone.GrabTrash.service.PaymentService;
import com.capstone.GrabTrash.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST Controller for handling driver-related endpoints
 * All endpoints require JWT authentication with driver role
 */
@RestController
@RequestMapping("/api/driver")
@PreAuthorize("hasRole('driver')")
public class DriverController {

    private final PaymentService paymentService;
    private final UserService userService;

    @Autowired
    public DriverController(PaymentService paymentService, UserService userService) {
        this.paymentService = paymentService;
        this.userService = userService;
    }

    /**
     * Get all payments assigned to the current authenticated driver
     * Requires JWT authentication with driver role
     * @return List of all payments assigned to the driver
     */
    @GetMapping("/payments")
    @PreAuthorize("hasRole('driver')")
    public ResponseEntity<List<PaymentResponseDTO>> getAssignedPayments() {
        return ResponseEntity.ok(paymentService.getAssignedPayments());
    }
    
    /**
     * Get all payments assigned to a specific driver
     * Requires JWT authentication with driver role
     * @param driverId Driver ID to fetch payments for
     * @return List of all payments assigned to the driver
     */
    @GetMapping("/{driverId}/payments")
    @PreAuthorize("hasRole('driver')")
    public ResponseEntity<List<PaymentResponseDTO>> getAssignedPaymentsByDriverId(@PathVariable String driverId) {
        return ResponseEntity.ok(paymentService.getAssignedPaymentsByDriverId(driverId));
    }
    
    /**
     * Update job order status
     * Requires JWT authentication with driver role
     * @param paymentId Payment/Job order ID
     * @param statusUpdate Status update information
     * @return Updated payment/job order
     */
    @PutMapping("/job/{paymentId}/status")
    @PreAuthorize("hasRole('driver')")
    public ResponseEntity<PaymentResponseDTO> updateJobOrderStatus(
            @PathVariable String paymentId,
            @RequestBody JobOrderStatusUpdateDTO statusUpdate) {
        return ResponseEntity.ok(paymentService.updateJobOrderStatus(paymentId, statusUpdate.getJobOrderStatus()));
    }
    
    /**
     * Helper method to get user ID from email
     * @param email User's email
     * @return User ID
     */
    private String getUserIdFromEmail(String email) {
        try {
            return userService.getUserIdByEmail(email);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Failed to retrieve user information: " + e.getMessage());
        }
    }
} 