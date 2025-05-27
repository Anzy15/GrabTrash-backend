package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.dto.DashboardStatsDTO;
import com.capstone.GrabTrash.dto.PaymentRequestDTO;
import com.capstone.GrabTrash.dto.PaymentResponseDTO;
import com.capstone.GrabTrash.model.Payment;
import com.capstone.GrabTrash.model.User;
import com.capstone.GrabTrash.model.Truck;
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
                    .wasteType(paymentRequest.getWasteType())
                    .jobOrderStatus("NEW")
                    .build();

            // Save the payment to Firestore
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);

            // Return the response
            return PaymentResponseDTO.builder()
                    .id(paymentId)
                    .orderId(payment.getOrderId())
                    .status(payment.getStatus())
                    .customerName(payment.getCustomerName())
                    .address(payment.getAddress())
                    .latitude(payment.getLatitude())
                    .longitude(payment.getLongitude())
                    .amount(payment.getAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentReference(payment.getPaymentReference())
                    .createdAt(payment.getCreatedAt())
                    .barangayId(barangayId)
                    .phoneNumber(phoneNumber)
                    .wasteType(payment.getWasteType())
                    .jobOrderStatus(payment.getJobOrderStatus())
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
                .amount(payment.getAmount())
                .tax(payment.getTax() != null ? payment.getTax() : 0.0)
                .totalAmount(payment.getTotalAmount() != null ? payment.getTotalAmount() : payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .barangayId(payment.getBarangayId())
                .customerName(payment.getCustomerName())
                .address(payment.getAddress())
                .latitude(payment.getLatitude() != null ? payment.getLatitude() : 0.0)
                .longitude(payment.getLongitude() != null ? payment.getLongitude() : 0.0)
                .phoneNumber(payment.getPhoneNumber())
                .driverId(payment.getDriverId())
                .wasteType(payment.getWasteType())
                .truckId(payment.getTruckId())
                .jobOrderStatus(payment.getJobOrderStatus())
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

    /**
     * Assign a driver to a payment
     * @param paymentId ID of the payment
     * @param driverId ID of the driver to assign
     * @return Updated payment information
     */
    public PaymentResponseDTO assignDriver(String paymentId, String driverId) {
        try {
            // Verify the payment exists
            Payment payment = firestore.collection(COLLECTION_NAME).document(paymentId).get().get().toObject(Payment.class);
            if (payment == null) {
                throw new RuntimeException("Payment not found with ID: " + paymentId);
            }

            // Verify the user exists and is a driver
            User driver = userService.getUserById(driverId);
            if (driver == null) {
                throw new RuntimeException("Driver not found with ID: " + driverId);
            }
            if (!"driver".equalsIgnoreCase(driver.getRole())) {
                throw new RuntimeException("User is not a driver");
            }

            // Update the payment with the driver ID
            payment.setDriverId(driverId);
            payment.setUpdatedAt(new Date());

            // Save the updated payment
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);

            // Return the updated payment information
            return mapToResponseDTO(payment);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error assigning driver to payment", e);
            throw new RuntimeException("Failed to assign driver: " + e.getMessage());
        }
    }

    /**
     * Get all payments assigned to a specific driver
     * @param driverId Driver's user ID
     * @return List of payments assigned to the driver
     */
    public List<PaymentResponseDTO> getPaymentsByDriverId(String driverId) {
        try {
            CollectionReference paymentsCollection = firestore.collection(COLLECTION_NAME);
            Query query = paymentsCollection.whereEqualTo("driverId", driverId);
            ApiFuture<QuerySnapshot> future = query.get();
            List<Payment> payments = future.get().toObjects(Payment.class);
            return mapToResponseDTOList(payments);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting payments by driverId", e);
            throw new RuntimeException("Failed to get payments for driver: " + e.getMessage());
        }
    }

    /**
     * Assign a truck to a payment
     * @param paymentId Payment ID
     * @param truckId Truck ID
     * @return Updated payment response
     */
    public PaymentResponseDTO assignTruckToPayment(String paymentId, String truckId) {
        try {
            // Get the payment
            Payment payment = firestore.collection(COLLECTION_NAME).document(paymentId).get().get().toObject(Payment.class);
            if (payment == null) {
                throw new RuntimeException("Payment not found with ID: " + paymentId);
            }
            
            // Get the truck and verify it's available
            Truck truck = firestore.collection("trucks").document(truckId).get().get().toObject(Truck.class);
            if (truck == null) {
                throw new RuntimeException("Truck not found with ID: " + truckId);
            }
            
            // Check if the truck is available
            if (truck.getStatus() != null && !truck.getStatus().equals("AVAILABLE")) {
                throw new RuntimeException("Truck is not available. Current status: " + truck.getStatus());
            }
            
            // Update the payment with the truck ID
            payment.setTruckId(truckId);
            payment.setUpdatedAt(new Date());
            
            // Update job order status to IN_PROGRESS if currently NEW
            if (payment.getJobOrderStatus() == null || "NEW".equals(payment.getJobOrderStatus())) {
                payment.setJobOrderStatus("IN_PROGRESS");
            }
            
            // Save the updated payment to Firestore
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);
            
            // Update the truck status to "CURRENTLY_IN_USE"
            truck.setStatus("CURRENTLY_IN_USE");
            truck.setUpdatedAt(new Date());
            
            // Save the updated truck to Firestore
            firestore.collection("trucks").document(truckId).set(truck);
            
            return mapToResponseDTO(payment);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error assigning truck to payment", e);
            throw new RuntimeException("Failed to assign truck to payment: " + e.getMessage());
        }
    }

    /**
     * Release a truck from a payment (mark it as available again)
     * @param paymentId Payment ID
     * @return Updated payment response
     */
    public PaymentResponseDTO releaseTruckFromPayment(String paymentId) {
        try {
            // Get the payment
            Payment payment = firestore.collection(COLLECTION_NAME).document(paymentId).get().get().toObject(Payment.class);
            if (payment == null) {
                throw new RuntimeException("Payment not found with ID: " + paymentId);
            }
            
            // Check if the payment has an assigned truck
            String truckId = payment.getTruckId();
            if (truckId == null || truckId.isEmpty()) {
                throw new RuntimeException("No truck assigned to this payment");
            }
            
            // Get the truck
            Truck truck = firestore.collection("trucks").document(truckId).get().get().toObject(Truck.class);
            if (truck == null) {
                throw new RuntimeException("Truck not found with ID: " + truckId);
            }
            
            // Update the truck status to "AVAILABLE"
            truck.setStatus("AVAILABLE");
            truck.setUpdatedAt(new Date());
            
            // Save the updated truck to Firestore
            firestore.collection("trucks").document(truckId).set(truck);
            
            // Mark payment as COMPLETED
            payment.setStatus("COMPLETED");
            payment.setUpdatedAt(new Date());
            
            // Save the updated payment
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);
            
            return mapToResponseDTO(payment);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error releasing truck from payment", e);
            throw new RuntimeException("Failed to release truck: " + e.getMessage());
        }
    }

    /**
     * Update job order status by a driver
     * @param paymentId Payment ID
     * @param jobOrderStatus New job order status (e.g., "COMPLETED")
     * @return Updated payment response
     */
    public PaymentResponseDTO updateJobOrderStatus(String paymentId, String jobOrderStatus) {
        try {
            // Get the payment
            Payment payment = firestore.collection(COLLECTION_NAME).document(paymentId).get().get().toObject(Payment.class);
            if (payment == null) {
                throw new RuntimeException("Payment not found with ID: " + paymentId);
            }
            
            // Security check: verify the current user is the driver assigned to this job
            // This would typically use SecurityContextHolder to get the current user
            // For now, we'll rely on the security at controller level with @PreAuthorize
            
            // Validate job order status
            if (jobOrderStatus == null || jobOrderStatus.isEmpty()) {
                throw new RuntimeException("Job order status cannot be empty");
            }
            
            // Store previous status for comparison
            String previousStatus = payment.getJobOrderStatus();
            
            // Update job order status
            payment.setJobOrderStatus(jobOrderStatus);
            payment.setUpdatedAt(new Date());
            
            // If job is marked as COMPLETED, release the truck (set to available)
            if ("COMPLETED".equalsIgnoreCase(jobOrderStatus) && payment.getTruckId() != null) {
                String truckId = payment.getTruckId();
                Truck truck = firestore.collection("trucks").document(truckId).get().get().toObject(Truck.class);
                
                if (truck != null) {
                    // Update truck status to AVAILABLE
                    truck.setStatus("AVAILABLE");
                    truck.setUpdatedAt(new Date());
                    firestore.collection("trucks").document(truckId).set(truck);
                }
                
                // Also update payment status
                payment.setStatus("COMPLETED");
            }
            
            // Save the updated payment to Firestore
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);
            
            return mapToResponseDTO(payment);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error updating job order status", e);
            throw new RuntimeException("Failed to update job order status: " + e.getMessage());
        }
    }
}