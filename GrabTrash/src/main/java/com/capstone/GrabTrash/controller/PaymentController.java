package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.dto.DashboardStatsDTO;
import com.capstone.GrabTrash.dto.PaymentRequestDTO;
import com.capstone.GrabTrash.dto.PaymentResponseDTO;
import com.capstone.GrabTrash.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}