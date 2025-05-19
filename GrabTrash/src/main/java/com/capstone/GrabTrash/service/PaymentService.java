package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.dto.DashboardStatsDTO;
import com.capstone.GrabTrash.dto.PaymentRequestDTO;
import com.capstone.GrabTrash.dto.PaymentResponseDTO;
import com.capstone.GrabTrash.model.Payment;
import com.capstone.GrabTrash.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Service for handling payment-related operations
 */
@Service
@Slf4j
public class PaymentService {

    private static final String COLLECTION_NAME = "payments";

    private final Firestore firestore;
    private final UserService userService;

    @Autowired
    public PaymentService(Firestore firestore, @Lazy UserService userService) {
        this.firestore = firestore;
        this.userService = userService;
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

            // Fetch user to get barangayId and phoneNumber
            String barangayId = null;
            String phoneNumber = null;
            if (paymentRequest.getCustomerEmail() != null) {
                User user = userService.getUserByEmailOrUsername(paymentRequest.getCustomerEmail());
                if (user != null) {
                    barangayId = user.getBarangayId();
                    phoneNumber = user.getPhoneNumber();
                }
            }
            if (paymentRequest.getBarangayId() != null) {
                barangayId = paymentRequest.getBarangayId(); // allow override if provided
            }

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
                    .barangayId(barangayId)
                    .phoneNumber(phoneNumber)
                    .build();

            // Save the payment to Firestore
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);

            // Return the response
            return PaymentResponseDTO.builder()
                    .id(paymentId)
                    .orderId(payment.getOrderId())
                    .status(payment.getStatus())
                    .address(payment.getAddress())
                    .latitude(payment.getLatitude())
                    .longitude(payment.getLongitude())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentReference(payment.getPaymentReference())
                    .createdAt(payment.getCreatedAt())
                    .barangayId(barangayId)
                    .phoneNumber(phoneNumber)
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
     * Get dashboard statistics including total pickup trash ordered
     * @return Dashboard statistics
     */
    public DashboardStatsDTO getDashboardStats() {
        try {
            CollectionReference paymentsCollection = firestore.collection(COLLECTION_NAME);
            
            // Get all completed payments
            Query query = paymentsCollection.whereEqualTo("status", "COMPLETED");
            ApiFuture<QuerySnapshot> future = query.get();
            List<Payment> payments = future.get().toObjects(Payment.class);
            
            // Calculate total pickup trash ordered (count of completed orders)
            int totalPickupTrashOrdered = payments.size();
            
            // Calculate total revenue from all completed orders
            double totalRevenue = payments.stream()
                    .mapToDouble(Payment::getTotalAmount)
                    .sum();
            
            // Count of completed orders is the same as totalPickupTrashOrdered in this case
            int totalCompletedOrders = totalPickupTrashOrdered;
            
            // Create and return the dashboard stats DTO
            return DashboardStatsDTO.builder()
                    .totalPickupTrashOrdered(totalPickupTrashOrdered)
                    .totalRevenue(totalRevenue)
                    .totalCompletedOrders(totalCompletedOrders)
                    .message("Dashboard statistics retrieved successfully")
                    .build();
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting dashboard statistics", e);
            throw new RuntimeException("Failed to get dashboard statistics: " + e.getMessage());
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
                .barangayId(payment.getBarangayId())
                .address(payment.getAddress())
                .latitude(payment.getLatitude() != null ? payment.getLatitude() : 0.0)
                .longitude(payment.getLongitude() != null ? payment.getLongitude() : 0.0)
                .phoneNumber(payment.getPhoneNumber())
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

    public List<Map<String, Object>> getTopBarangaysByPickupFrequency(int topN) {
        try {
            CollectionReference paymentsCollection = firestore.collection(COLLECTION_NAME);
            Query query = paymentsCollection.whereEqualTo("status", "COMPLETED");
            ApiFuture<QuerySnapshot> future = query.get();
            List<Payment> payments = future.get().toObjects(Payment.class);

            // Count by barangayId
            Map<String, Long> barangayCounts = payments.stream()
                .filter(p -> p.getBarangayId() != null)
                .collect(Collectors.groupingBy(Payment::getBarangayId, Collectors.counting()));

            // Sort and get top N
            List<Map<String, Object>> topBarangays = barangayCounts.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(topN)
                .map(e -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("barangayId", e.getKey());
                    map.put("count", e.getValue());
                    return map;
                })
                .collect(Collectors.toList());

            return topBarangays;
        } catch (Exception e) {
            log.error("Error getting top barangays by pickup frequency", e);
            throw new RuntimeException("Failed to get top barangays: " + e.getMessage());
        }
    }
}