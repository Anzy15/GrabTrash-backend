package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.dto.PaymentRequestDTO;
import com.capstone.GrabTrash.dto.PaymentResponseDTO;
import com.capstone.GrabTrash.model.Payment;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Service for handling payment-related operations
 */
@Service
@Slf4j
public class PaymentService {

    private static final String COLLECTION_NAME = "payments";

    private final Firestore firestore;

    @Autowired
    public PaymentService(Firestore firestore) {
        this.firestore = firestore;
    }

    /**
     * Process a new payment from the mobile app
     * @param paymentRequest Payment information from the mobile app
     * @return Payment confirmation response
     */
    public PaymentResponseDTO processPayment(PaymentRequestDTO paymentRequest) {
        try {
            // Create a new payment record
            String paymentId = UUID.randomUUID().toString();

            Payment payment = Payment.builder()
                    .id(paymentId)
                    .orderId(paymentRequest.getOrderId())
                    .customerName(paymentRequest.getCustomerName())
                    .customerEmail(paymentRequest.getCustomerEmail())
                    .address(paymentRequest.getAddress())
                    .latitude(paymentRequest.getLatitude())
                    .longitude(paymentRequest.getLongitude())
                    .amount(paymentRequest.getAmount())
                    .tax(paymentRequest.getTax())
                    .totalAmount(paymentRequest.getTotalAmount())
                    .paymentMethod(paymentRequest.getPaymentMethod())
                    .paymentReference(paymentRequest.getPaymentReference())
                    .notes(paymentRequest.getNotes())
                    .status("COMPLETED")
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            // Save the payment to Firestore
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);

            // Return the response
            return PaymentResponseDTO.builder()
                    .id(paymentId)
                    .orderId(payment.getOrderId())
                    .status(payment.getStatus())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentReference(payment.getPaymentReference())
                    .createdAt(payment.getCreatedAt())
                    .message("Payment processed successfully")
                    .build();

        } catch (Exception e) {
            log.error("Error processing payment", e);
            throw new RuntimeException("Failed to process payment: " + e.getMessage());
        }
    }

    /**
     * Get all payments
     * @return List of all payments
     */
    public List<PaymentResponseDTO> getAllPayments() {
        try {
            CollectionReference paymentsCollection = firestore.collection(COLLECTION_NAME);

            ApiFuture<QuerySnapshot> future = paymentsCollection.get();
            List<Payment> payments = future.get().toObjects(Payment.class);

            return mapToResponseDTOList(payments);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting all payments", e);
            throw new RuntimeException("Failed to get payments: " + e.getMessage());
        }
    }

    /**
     * Get payment by ID
     * @param id Payment ID
     * @return Payment information
     */
    public PaymentResponseDTO getPaymentById(String id) {
        try {
            Payment payment = firestore.collection(COLLECTION_NAME).document(id).get().get().toObject(Payment.class);

            if (payment == null) {
                throw new RuntimeException("Payment not found with ID: " + id);
            }

            return mapToResponseDTO(payment);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting payment by ID", e);
            throw new RuntimeException("Failed to get payment: " + e.getMessage());
        }
    }

    /**
     * Get payment by order ID
     * @param orderId Order ID
     * @return Payment information
     */
    public PaymentResponseDTO getPaymentByOrderId(String orderId) {
        try {
            CollectionReference paymentsCollection = firestore.collection(COLLECTION_NAME);

            Query query = paymentsCollection.whereEqualTo("orderId", orderId);
            ApiFuture<QuerySnapshot> future = query.get();
            List<Payment> payments = future.get().toObjects(Payment.class);

            if (payments.isEmpty()) {
                throw new RuntimeException("Payment not found with order ID: " + orderId);
            }

            return mapToResponseDTO(payments.get(0));

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting payment by order ID", e);
            throw new RuntimeException("Failed to get payment: " + e.getMessage());
        }
    }

    /**
     * Get payments by customer email
     * @param email Customer email
     * @return List of payments for the customer
     */
    public List<PaymentResponseDTO> getPaymentsByCustomerEmail(String email) {
        try {
            CollectionReference paymentsCollection = firestore.collection(COLLECTION_NAME);

            Query query = paymentsCollection.whereEqualTo("customerEmail", email);
            ApiFuture<QuerySnapshot> future = query.get();
            List<Payment> payments = future.get().toObjects(Payment.class);

            return mapToResponseDTOList(payments);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting payments by customer email", e);
            throw new RuntimeException("Failed to get payments: " + e.getMessage());
        }
    }

    /**
     * Map a Payment entity to a PaymentResponseDTO
     * @param payment Payment entity
     * @return PaymentResponseDTO
     */
    private PaymentResponseDTO mapToResponseDTO(Payment payment) {
        return PaymentResponseDTO.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .paymentReference(payment.getPaymentReference())
                .createdAt(payment.getCreatedAt())
                .message("Payment retrieved successfully")
                .build();
    }

    /**
     * Map a list of Payment entities to a list of PaymentResponseDTOs
     * @param payments List of Payment entities
     * @return List of PaymentResponseDTOs
     */
    private List<PaymentResponseDTO> mapToResponseDTOList(List<Payment> payments) {
        List<PaymentResponseDTO> responseDTOs = new ArrayList<>();
        for (Payment payment : payments) {
            responseDTOs.add(mapToResponseDTO(payment));
        }
        return responseDTOs;
    }
}