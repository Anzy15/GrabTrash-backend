package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.dto.DashboardStatsDTO;
import com.capstone.GrabTrash.dto.PaymentRequestDTO;
import com.capstone.GrabTrash.dto.PaymentResponseDTO;
import com.capstone.GrabTrash.dto.DriverAssignmentDTO;
import com.capstone.GrabTrash.dto.DeliveryStatusUpdateDTO;
import com.capstone.GrabTrash.dto.JobOrderStatusUpdateDTO;
import com.capstone.GrabTrash.dto.ImageConfirmationDTO;
import com.capstone.GrabTrash.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for handling payment-related endpoints
 * All endpoints require JWT authentication
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Process a new payment from the mobile app
     * Requires JWT authentication in the Authorization header
     * @param paymentRequest Payment information from the mobile app
     * @return Payment confirmation response
     */
    @PostMapping
    public ResponseEntity<PaymentResponseDTO> processPayment(@RequestBody PaymentRequestDTO paymentRequest) {
        PaymentResponseDTO response = paymentService.processPayment(paymentRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all payments
     * Requires JWT authentication in the Authorization header
     * @return List of all payments
     */
    @GetMapping
    public ResponseEntity<List<PaymentResponseDTO>> getAllPayments() {
        List<PaymentResponseDTO> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payment by ID
     * Requires JWT authentication in the Authorization header
     * @param id Payment ID
     * @return Payment information
     */
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponseDTO> getPaymentById(@PathVariable String id) {
        PaymentResponseDTO payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    /**
     * Get payment by order ID
     * Requires JWT authentication in the Authorization header
     * @param orderId Order ID
     * @return Payment information
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDTO> getPaymentByOrderId(@PathVariable String orderId) {
        PaymentResponseDTO payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }

    /**
     * Get payments by customer email
     * Requires JWT authentication in the Authorization header
     * @param email Customer email
     * @return List of payments for the customer
     */
    @GetMapping("/customer")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByCustomerEmail(@RequestParam String email) {
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByCustomerEmail(email);
        return ResponseEntity.ok(payments);
    }
    
    /**
     * Get dashboard statistics including total pickup trash ordered
     * Requires JWT authentication in the Authorization header
     * @return Dashboard statistics including total pickup trash ordered
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        DashboardStatsDTO stats = paymentService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get top 3 barangays by pickup frequency
     */
    @GetMapping("/top-barangays")
    public ResponseEntity<List<Map<String, Object>>> getTopBarangays() {
        List<Map<String, Object>> topBarangays = paymentService.getTopBarangaysByPickupFrequency(3);
        return ResponseEntity.ok(topBarangays);
    }

    /**
     * Assign a driver to a payment
     * Requires JWT authentication in the Authorization header
     * @param assignment Driver assignment information
     * @return Updated payment information
     */
    @PostMapping("/assign-driver")
    public ResponseEntity<PaymentResponseDTO> assignDriver(@RequestBody DriverAssignmentDTO assignment) {
        PaymentResponseDTO updatedPayment = paymentService.assignDriver(assignment.getPaymentId(), assignment.getDriverId());
        return ResponseEntity.ok(updatedPayment);
    }

    /**
     * Get all payments assigned to a specific driver
     * Requires JWT authentication in the Authorization header
     * @param driverId Driver's user ID
     * @return List of payments assigned to the driver
     */
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<PaymentResponseDTO>> getPaymentsByDriverId(@PathVariable String driverId) {
        List<PaymentResponseDTO> payments = paymentService.getPaymentsByDriverId(driverId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Update the delivery status of a payment
     * Requires JWT authentication with admin or driver role
     * @param paymentId Payment ID
     * @param updateRequest Delivery status update request
     * @return Updated payment response
     */
    @PutMapping("/{paymentId}/delivery-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'DRIVER')")
    public ResponseEntity<PaymentResponseDTO> updateDeliveryStatus(
            @PathVariable String paymentId,
            @RequestBody DeliveryStatusUpdateDTO updateRequest) {
        
        PaymentResponseDTO updatedPayment = paymentService.updateDeliveryStatus(
                paymentId, 
                updateRequest.getIsDelivered());
        
        return ResponseEntity.ok(updatedPayment);
    }

    /**
     * Update the job order status of a payment by customer or driver
     * Requires JWT authentication with customer or driver role
     * Customers can only update their own payments, drivers can only update assigned payments
     * @param paymentId Payment ID
     * @param updateRequest Job order status update request
     * @return Updated payment response
     */
    @PutMapping("/{paymentId}/job-order-status")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DRIVER')")
    public ResponseEntity<PaymentResponseDTO> updateJobOrderStatus(
            @PathVariable String paymentId,
            @RequestBody JobOrderStatusUpdateDTO updateRequest) {
        
        PaymentResponseDTO updatedPayment = paymentService.updateJobOrderStatusByRole(
                paymentId, 
                updateRequest.getJobOrderStatus());
        
        return ResponseEntity.ok(updatedPayment);
    }

    /**
     * Upload image confirmation proof for job status
     * Allows both customers and drivers to upload confirmation images
     * Requires JWT authentication with customer or driver role
     * @param paymentId Payment ID
     * @param imageRequest Image confirmation request
     * @return Updated payment response
     */
    @PostMapping("/{paymentId}/confirmation-image")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'DRIVER')")
    public ResponseEntity<PaymentResponseDTO> uploadConfirmationImage(
            @PathVariable String paymentId,
            @RequestBody ImageConfirmationDTO imageRequest) {
        
        PaymentResponseDTO updatedPayment = paymentService.uploadConfirmationImage(
                paymentId, 
                imageRequest.getImageUrl());
        
        return ResponseEntity.ok(updatedPayment);
    }
}