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
@PreAuthorize("hasRole('DRIVER')")
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
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<PaymentResponseDTO>> getAssignedPayments() {
        // Get the current authenticated driver's ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String driverEmail = authentication.getName(); // This is the driver's email
        
        // Get driver's ID from email
        String driverId = getUserIdFromEmail(driverEmail);
        
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByDriverId(driverId);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get all payments assigned to a specific driver
     * Requires JWT authentication with driver role
     * @param driverId Driver ID to fetch payments for
     * @return List of all payments assigned to the driver
     */
    @GetMapping("/{driverId}/payments")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<List<PaymentResponseDTO>> getAssignedPaymentsByDriverId(@PathVariable String driverId) {
        // Get the current authenticated driver's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String driverEmail = authentication.getName();
        
        // Get authenticated driver's ID from email
        String authenticatedDriverId = getUserIdFromEmail(driverEmail);
        
        // Security check: verify driver can only access their own payments
        if (!authenticatedDriverId.equals(driverId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "You are not authorized to view another driver's payments");
        }
        
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByDriverId(driverId);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Update job order status
     * Requires JWT authentication with driver role
     * @param paymentId Payment/Job order ID
     * @param statusUpdate Status update information
     * @return Updated payment/job order
     */
    @PutMapping("/job/{paymentId}/status")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<PaymentResponseDTO> updateJobOrderStatus(
            @PathVariable String paymentId,
            @RequestBody JobOrderStatusUpdateDTO statusUpdate) {
        
        // Get the current authenticated driver's email
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String driverEmail = authentication.getName();
        
        // Get driver's ID from email
        String driverId = getUserIdFromEmail(driverEmail);
        
        // Get the payment
        PaymentResponseDTO payment = paymentService.getPaymentById(paymentId);
        
        // Verify the payment is assigned to this driver
        if (!driverId.equals(payment.getDriverId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "You are not authorized to update this job order. It is assigned to another driver.");
        }
        
        // Update the job order status
        PaymentResponseDTO response = paymentService.updateJobOrderStatus(
                paymentId, 
                statusUpdate.getJobOrderStatus());
                
        return ResponseEntity.ok(response);
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