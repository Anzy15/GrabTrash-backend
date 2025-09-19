package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.dto.DashboardStatsDTO;
import com.capstone.GrabTrash.dto.PaymentRequestDTO;
import com.capstone.GrabTrash.dto.PaymentResponseDTO;
import com.capstone.GrabTrash.dto.TruckResponseDTO;
import com.capstone.GrabTrash.model.Payment;
import com.capstone.GrabTrash.model.User;
import com.capstone.GrabTrash.model.Truck;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
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
    private final TruckService truckService;
    private final NotificationService notificationService;
    private ListenerRegistration paymentsListener;

    @Autowired
    public PaymentService(Firestore firestore, @Lazy UserService userService, TruckService truckService, NotificationService notificationService) {
        this.firestore = firestore;
        this.userService = userService;
        this.truckService = truckService;
        this.notificationService = notificationService;
        
        // Initialize the Firestore listener for payments collection
        initializePaymentsListener();
    }

    /**
     * Initialize Firestore listener for payments collection to automatically detect status changes
     */
    private void initializePaymentsListener() {
        try {
            log.info("Initializing Firestore listener for payments collection");
            
            // Create a listener for the payments collection
            paymentsListener = firestore.collection(COLLECTION_NAME)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        log.error("Error listening for payment changes: {}", e.getMessage(), e);
                        return;
                    }
                    
                    if (snapshots == null || snapshots.isEmpty()) {
                        log.debug("No payments found in snapshot");
                        return;
                    }
                    
                    log.debug("Received snapshot with {} changes", snapshots.getDocumentChanges().size());
                    
                    // Process each document change
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        // We're only interested in modifications (not new documents or deletions)
                        if (dc.getType() == DocumentChange.Type.MODIFIED) {
                            Payment payment = dc.getDocument().toObject(Payment.class);
                            
                            if (payment != null) {
                                log.debug("Payment modified: {}, status: {}", payment.getId(), payment.getJobOrderStatus());
                                
                                // Check if the status is "Accepted" and handle notification
                                String normalizedStatus = normalizeStatus(payment.getJobOrderStatus());
                                if ("Accepted".equals(normalizedStatus)) {
                                    // Get the previous version of the document to check if status actually changed
                                    try {
                                        // Check if we've already sent a notification for this payment acceptance
                                        String notificationKey = "notification_sent_" + payment.getId();
                                        String cacheValue = getNotificationCache(notificationKey);
                                        
                                        if (cacheValue != null && cacheValue.equals("Accepted")) {
                                            log.debug("Notification already sent for payment ID: {}, skipping", payment.getId());
                                            continue;
                                        }
                                        
                                        log.info("Detected job order status change to Accepted for payment ID: {}", payment.getId());
                                        sendAcceptedNotification(payment);
                                        
                                        // Mark this notification as sent to avoid duplicates
                                        setNotificationCache(notificationKey, "Accepted");
                                    } catch (Exception ex) {
                                        log.error("Error processing status change for payment {}: {}", 
                                            payment.getId(), ex.getMessage(), ex);
                                    }
                                }
                            }
                        }
                    }
                });
                
            log.info("Firestore listener for payments collection initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Firestore listener: {}", e.getMessage(), e);
        }
    }
    
    // Simple in-memory cache to avoid duplicate notifications
    private final Map<String, String> notificationCache = new HashMap<>();
    
    private String getNotificationCache(String key) {
        return notificationCache.get(key);
    }
    
    private void setNotificationCache(String key, String value) {
        notificationCache.put(key, value);
    }
    
    /**
     * Send notification for a payment that has been accepted
     * @param payment The payment that was accepted
     */
    private void sendAcceptedNotification(Payment payment) {
        try {
            if (payment.getCustomerEmail() == null) {
                log.warn("No customer email associated with payment: {}", payment.getId());
                return;
            }
            
            log.debug("Looking up customer by email: {}", payment.getCustomerEmail());
            User customer = userService.getUserByEmailOrUsername(payment.getCustomerEmail());
            
            if (customer == null) {
                log.warn("Customer not found for email: {}", payment.getCustomerEmail());
                return;
            }
            
            log.debug("Found customer: {}, userId: {}", customer.getEmail(), customer.getUserId());
            
            // Check if customer has a valid FCM token
            boolean hasValidToken = notificationService.hasValidFcmToken(customer.getUserId());
            if (!hasValidToken) {
                log.warn("Customer does not have a valid FCM token, notification will not be sent");
                return;
            }
            
            Map<String, String> data = new HashMap<>();
            data.put("paymentId", payment.getId());
            data.put("type", "JOB_ORDER_ACCEPTED");
            
            boolean notificationSent = notificationService.sendNotificationToUser(
                customer.getUserId(),
                "Job Order Accepted",
                "Your job order has been accepted by the driver",
                data
            );
            
            log.info("Notification sent to customer: {}", notificationSent ? "success" : "failed");
        } catch (Exception e) {
            log.error("Error sending accepted notification for payment {}: {}", payment.getId(), e.getMessage(), e);
        }
    }
    
    /**
     * Cleanup method to close the listener when the service is destroyed
     */
    @PreDestroy
    public void cleanup() {
        if (paymentsListener != null) {
            log.info("Closing Firestore payments listener");
            paymentsListener.remove();
        }
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
            
            // AUTOMATED TRUCK AND DRIVER ASSIGNMENT
            String assignedTruckId = null;
            String assignedDriverId = null;
            Double calculatedAmount = paymentRequest.getAmount(); // Default to request amount
            Double calculatedTotalAmount = paymentRequest.getTotalAmount(); // Default to request total
            
            // Only attempt auto-assignment if trashWeight is provided
            if (paymentRequest.getTrashWeight() != null && paymentRequest.getTrashWeight() > 0) {
                log.info("Attempting automated truck assignment for weight: {} kg, waste type: {}", 
                    paymentRequest.getTrashWeight(), paymentRequest.getWasteType());
                
                try {
                    // Find available trucks that can handle the weight
                    List<Truck> availableTrucks = truckService.findAvailableTrucksByCapacity(
                        paymentRequest.getTrashWeight(), 
                        paymentRequest.getWasteType()
                    );
                    
                    log.info("Found {} available trucks for assignment", availableTrucks.size());
                    
                    if (!availableTrucks.isEmpty()) {
                        // Select the first truck (smallest sufficient capacity due to sorting)
                        Truck selectedTruck = availableTrucks.get(0);
                        assignedTruckId = selectedTruck.getTruckId();
                        assignedDriverId = selectedTruck.getDriverId();
                        
                        // Calculate amount based on truck price
                        if (selectedTruck.getTruckPrice() != null && selectedTruck.getTruckPrice() > 0) {
                            calculatedAmount = selectedTruck.getTruckPrice();
                            calculatedTotalAmount = selectedTruck.getTruckPrice(); // Use base truck price without service fee
                            
                            log.info("Using truck base price - Amount: {}, Total Amount: {} based on truck price: {}", 
                                calculatedAmount, calculatedTotalAmount, selectedTruck.getTruckPrice());
                        } else {
                            log.warn("Selected truck {} has no price set, using request amounts", assignedTruckId);
                        }
                        
                        log.info("Auto-assigned truck: {} (capacity: {} kg, price: {}) and driver: {} for payment: {}", 
                            assignedTruckId, selectedTruck.getCapacity(), selectedTruck.getTruckPrice(), assignedDriverId, paymentId);
                    } else {
                        log.warn("No available trucks found for weight: {} kg and waste type: {}. Payment will be created without assignment.", 
                            paymentRequest.getTrashWeight(), paymentRequest.getWasteType());
                        
                        // Debug: Try to find ALL trucks to see what's available
                        try {
                            List<TruckResponseDTO> allTrucks = truckService.getAllTrucks();
                            log.info("Total trucks in system: {}", allTrucks.size());
                            for (TruckResponseDTO truck : allTrucks) {
                                log.debug("Truck {}: capacity={}, status={}, driverId={}, wasteType={}, price={}", 
                                    truck.getTruckId(), truck.getCapacity(), truck.getStatus(), 
                                    truck.getDriverId(), truck.getWasteType(), truck.getTruckPrice());
                            }
                        } catch (Exception debugEx) {
                            log.warn("Could not fetch all trucks for debugging: {}", debugEx.getMessage());
                        }
                    }
                } catch (Exception e) {
                    log.error("Error during automated truck assignment: {}", e.getMessage(), e);
                    // Continue with payment creation even if auto-assignment fails
                }
            } else {
                log.info("Skipping automated truck assignment - trashWeight not provided or invalid: {}", 
                    paymentRequest.getTrashWeight());
            }

            Payment payment = Payment.builder()
                    .id(paymentId)
                    .orderId(paymentRequest.getOrderId())
                    .customerName(paymentRequest.getCustomerName())
                    .customerEmail(paymentRequest.getCustomerEmail())
                    .address(paymentRequest.getAddress())
                    .latitude(paymentRequest.getLatitude())
                    .longitude(paymentRequest.getLongitude())
                    .amount(calculatedAmount) // Use calculated amount based on truck price
                    .totalAmount(calculatedTotalAmount) // Use calculated total amount
                    .paymentMethod(paymentRequest.getPaymentMethod())
                    .paymentReference(paymentRequest.getPaymentReference())
                    .notes(paymentRequest.getNotes())
                    .status("COMPLETED")
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .barangayId(barangayId)
                    .phoneNumber(phoneNumber)
                    .wasteType(paymentRequest.getWasteType())
                    .trashWeight(paymentRequest.getTrashWeight())
                    .truckId(assignedTruckId != null ? assignedTruckId : paymentRequest.getTruckId())
                    .driverId(assignedDriverId)
                    .jobOrderStatus(assignedTruckId != null ? "NEW" : "Available")
                    .build();

            // Save the payment to Firestore
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);
            
            // Send notification to assigned driver if auto-assignment was successful
            if (assignedDriverId != null) {
                try {
                    Map<String, String> notificationData = new HashMap<>();
                    notificationData.put("paymentId", paymentId);
                    notificationData.put("type", "NEW_JOB_ASSIGNMENT");
                    
                    notificationService.sendNotificationToUser(
                        assignedDriverId,
                        "New Job Assignment",
                        "You have been automatically assigned to a new pickup order at " + payment.getAddress(),
                        notificationData
                    );
                    
                    log.info("Notification sent to assigned driver: {}", assignedDriverId);
                } catch (Exception e) {
                    log.error("Failed to send notification to driver: {}", e.getMessage(), e);
                    // Don't fail the payment creation if notification fails
                }
            }

            // Return the response
            return PaymentResponseDTO.builder()
                    .id(paymentId)
                    .orderId(payment.getOrderId())
                    .status(payment.getStatus())
                    .customerName(payment.getCustomerName())
                    .customerEmail(payment.getCustomerEmail())
                    .address(payment.getAddress())
                    .latitude(payment.getLatitude())
                    .longitude(payment.getLongitude())
                    .amount(payment.getAmount())
                    .totalAmount(payment.getTotalAmount())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentReference(payment.getPaymentReference())
                    .createdAt(payment.getCreatedAt())
                    .barangayId(barangayId)
                    .phoneNumber(phoneNumber)
                    .wasteType(payment.getWasteType())
                    .trashWeight(payment.getTrashWeight())
                    .truckId(payment.getTruckId())
                    .driverId(payment.getDriverId())
                    .jobOrderStatus(payment.getJobOrderStatus())
                    .message(assignedTruckId != null ? 
                        String.format("Payment processed successfully with automated truck and driver assignment. Amount set to truck base price: %.2f", calculatedAmount) : 
                        "Payment processed successfully")
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
                .totalAmount(payment.getTotalAmount() != null ? payment.getTotalAmount() : payment.getAmount())
                .createdAt(payment.getCreatedAt())
                .barangayId(payment.getBarangayId())
                .customerName(payment.getCustomerName())
                .customerEmail(payment.getCustomerEmail())
                .address(payment.getAddress())
                .latitude(payment.getLatitude() != null ? payment.getLatitude() : 0.0)
                .longitude(payment.getLongitude() != null ? payment.getLongitude() : 0.0)
                .phoneNumber(payment.getPhoneNumber())
                .driverId(payment.getDriverId())
                .wasteType(payment.getWasteType())
                .trashWeight(payment.getTrashWeight())
                .truckId(payment.getTruckId())
                .jobOrderStatus(payment.getJobOrderStatus())
                .isDelivered(payment.getIsDelivered())
                .customerConfirmation(payment.getCustomerConfirmation())
                .driverConfirmation(payment.getDriverConfirmation())
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

            // Send notification to the driver
            Map<String, String> data = new HashMap<>();
            data.put("paymentId", paymentId);
            data.put("type", "DRIVER_ASSIGNMENT");
            
            notificationService.sendNotificationToUser(
                driverId,
                "New Job Assignment",
                "You have been assigned to a new job order at " + payment.getAddress(),
                data
            );

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
     * @param jobOrderStatus New job order status (e.g., "Completed")
     * @return Updated payment response
     */
    public PaymentResponseDTO updateJobOrderStatus(String paymentId, String jobOrderStatus) {
        try {
            log.info("Updating job order status for payment ID: {} to status: {}", paymentId, jobOrderStatus);
            
            // Get the payment
            Payment payment = firestore.collection(COLLECTION_NAME).document(paymentId).get().get().toObject(Payment.class);
            if (payment == null) {
                log.error("Payment not found with ID: {}", paymentId);
                throw new RuntimeException("Payment not found with ID: " + paymentId);
            }
            
            log.debug("Found payment: {}, current status: {}", payment.getId(), payment.getJobOrderStatus());
            
            // Security check: verify the current user is the driver assigned to this job
            // This would typically use SecurityContextHolder to get the current user
            // For now, we'll rely on the security at controller level with @PreAuthorize
            
            // Validate job order status
            if (jobOrderStatus == null || jobOrderStatus.isEmpty()) {
                log.error("Job order status cannot be empty for payment ID: {}", paymentId);
                throw new RuntimeException("Job order status cannot be empty");
            }
            
            // Store previous status for comparison
            String previousStatus = payment.getJobOrderStatus();
            log.debug("Previous job order status: {}", previousStatus);
            
            // Normalize the job order status to ensure consistent casing
            // This helps with case-insensitive comparisons
            String normalizedStatus = normalizeStatus(jobOrderStatus);
            log.debug("Normalized new status: {}", normalizedStatus);
            
            // Update job order status
            payment.setJobOrderStatus(normalizedStatus);
            payment.setUpdatedAt(new Date());
            
            // If job is marked as Completed, release the truck (set to available)
            if ("Completed".equalsIgnoreCase(normalizedStatus) && payment.getTruckId() != null) {
                String truckId = payment.getTruckId();
                log.debug("Job marked as Completed, releasing truck: {}", truckId);
                
                Truck truck = firestore.collection("trucks").document(truckId).get().get().toObject(Truck.class);
                
                if (truck != null) {
                    // Update truck status to AVAILABLE
                    truck.setStatus("AVAILABLE");
                    truck.setUpdatedAt(new Date());
                    firestore.collection("trucks").document(truckId).set(truck);
                    log.debug("Updated truck status to AVAILABLE");
                }
                
                // Also update payment status
                payment.setStatus("COMPLETED");
            }
            
            // Save the updated payment
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);
            log.info("Successfully updated job order status to: {} for payment ID: {}", normalizedStatus, paymentId);
            
            // Send notification to the customer if the status changed to Accepted
            log.debug("Checking if notification should be sent. New status: {}, Previous status: {}, Equal: {}", 
                normalizedStatus, previousStatus, normalizedStatus.equalsIgnoreCase(previousStatus));
                
            if ("Accepted".equalsIgnoreCase(normalizedStatus) && !normalizedStatus.equalsIgnoreCase(previousStatus)) {
                log.info("Status changed to Accepted, sending notification to customer");
                
                if (payment.getCustomerEmail() != null) {
                    log.debug("Looking up customer by email: {}", payment.getCustomerEmail());
                    User customer = userService.getUserByEmailOrUsername(payment.getCustomerEmail());
                    
                    if (customer != null) {
                        log.debug("Found customer: {}, userId: {}", customer.getEmail(), customer.getUserId());
                        
                        // Check if customer has a valid FCM token
                        boolean hasValidToken = notificationService.hasValidFcmToken(customer.getUserId());
                        if (!hasValidToken) {
                            log.warn("Customer does not have a valid FCM token, notification will not be sent");
                            return mapToResponseDTO(payment);
                        }
                        
                        Map<String, String> data = new HashMap<>();
                        data.put("paymentId", paymentId);
                        data.put("type", "JOB_ORDER_ACCEPTED");
                        
                        boolean notificationSent = notificationService.sendNotificationToUser(
                            customer.getUserId(),
                            "Job Order Accepted",
                            "Your job order has been accepted by the driver",
                            data
                        );
                        
                        log.info("Notification sent to customer: {}", notificationSent ? "success" : "failed");
                    } else {
                        log.warn("Customer not found for email: {}", payment.getCustomerEmail());
                    }
                } else {
                    log.warn("No customer email associated with payment: {}", paymentId);
                }
            } else {
                log.debug("No notification needed. Status: {}, Previous: {}", normalizedStatus, previousStatus);
            }
            
            return mapToResponseDTO(payment);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error updating job order status for payment ID: {}", paymentId, e);
            throw new RuntimeException("Failed to update job order status: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to normalize job order status for consistent comparison
     * @param status The status to normalize
     * @return Normalized status in proper case format
     */
    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        
        // Don't convert to uppercase anymore
        String normalized = status;
        
        // Handle variations in formatting
        normalized = normalized.replace("-", "-");
        normalized = normalized.replace(" ", "-");
        
        // Make sure we're using consistent statuses
        switch (normalized) {
            case "New":
            case "Accepted":
            case "In-Progress":
            case "Completed":
            case "Cancelled":
                return normalized;
            case "Available":
                return "New"; // Map "Available" to "New"
            case "InProgress":
                return "In-Progress"; // Fix missing hyphen
            default:
                log.warn("Unknown job order status: {}, using as-is: {}", status, normalized);
                return normalized;
        }
    }

    /**
     * Update the delivery status of a payment
     * @param paymentId Payment ID
     * @param isDelivered New delivery status
     * @return Updated payment response
     */
    public PaymentResponseDTO updateDeliveryStatus(String paymentId, Boolean isDelivered) {
        try {
            log.info("Updating delivery status for payment ID: {} to: {}", paymentId, isDelivered);
            
            // Get the payment
            Payment payment = firestore.collection(COLLECTION_NAME).document(paymentId).get().get().toObject(Payment.class);
            if (payment == null) {
                log.error("Payment not found with ID: {}", paymentId);
                throw new RuntimeException("Payment not found with ID: " + paymentId);
            }
            
            // Update the delivery status
            payment.setIsDelivered(isDelivered);
            payment.setUpdatedAt(new Date());
            
            // Save the updated payment
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);
            log.info("Successfully updated delivery status to: {} for payment ID: {}", isDelivered, paymentId);
            
            return mapToResponseDTO(payment);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error updating delivery status for payment ID: {}", paymentId, e);
            throw new RuntimeException("Failed to update delivery status: " + e.getMessage());
        }
    }

    /**
     * Update job order status by a customer
     * This method includes authorization checks to ensure only the customer who owns the payment can update it
     * @param paymentId Payment ID
     * @param jobOrderStatus New job order status
     * @return Updated payment response
     */
    public PaymentResponseDTO updateJobOrderStatusByCustomer(String paymentId, String jobOrderStatus) {
        try {
            log.info("Customer updating job order status for payment ID: {} to status: {}", paymentId, jobOrderStatus);
            
            // Get the current user from security context
            String currentUserEmail = getCurrentUserEmail();
            if (currentUserEmail == null) {
                throw new RuntimeException("Unable to determine current user");
            }
            
            // Get the payment
            Payment payment = firestore.collection(COLLECTION_NAME).document(paymentId).get().get().toObject(Payment.class);
            if (payment == null) {
                log.error("Payment not found with ID: {}", paymentId);
                throw new RuntimeException("Payment not found with ID: " + paymentId);
            }
            
            // Authorization check: ensure the current user is the customer who owns this payment
            if (!currentUserEmail.equals(payment.getCustomerEmail())) {
                log.error("Unauthorized attempt to update payment. Current user: {}, Payment owner: {}", 
                    currentUserEmail, payment.getCustomerEmail());
                throw new RuntimeException("You are not authorized to update this payment");
            }
            
            log.debug("Authorization passed. Customer {} updating their payment {}", currentUserEmail, paymentId);
            
            // Validate job order status
            if (jobOrderStatus == null || jobOrderStatus.isEmpty()) {
                log.error("Job order status cannot be empty for payment ID: {}", paymentId);
                throw new RuntimeException("Job order status cannot be empty");
            }
            
            // Store previous status for comparison
            String previousStatus = payment.getJobOrderStatus();
            log.debug("Previous job order status: {}", previousStatus);
            
            // Normalize the job order status to ensure consistent casing
            String normalizedStatus = normalizeStatus(jobOrderStatus);
            log.debug("Normalized new status: {}", normalizedStatus);
            
            // Update job order status
            payment.setJobOrderStatus(normalizedStatus);
            payment.setUpdatedAt(new Date());
            
            // Save the updated payment
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);
            log.info("Successfully updated job order status to: {} for payment ID: {} by customer: {}", 
                normalizedStatus, paymentId, currentUserEmail);
            
            return mapToResponseDTO(payment);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error updating job order status for payment ID: {} by customer", paymentId, e);
            throw new RuntimeException("Failed to update job order status: " + e.getMessage());
        }
    }
    
    /**
     * Update job order status by customer or driver based on user role
     * Uses role-based authorization to determine access permissions
     * @param paymentId Payment ID
     * @param jobOrderStatus New job order status
     * @return Updated payment response
     */
    public PaymentResponseDTO updateJobOrderStatusByRole(String paymentId, String jobOrderStatus) {
        try {
            log.info("User updating job order status for payment ID: {} to status: {}", paymentId, jobOrderStatus);
            
            // Get the current user from security context
            String currentUserEmail = getCurrentUserEmail();
            String currentUserRole = getCurrentUserRole();
            
            if (currentUserEmail == null || currentUserRole == null) {
                throw new RuntimeException("Unable to determine current user or role");
            }
            
            log.debug("Current user: {}, role: {}", currentUserEmail, currentUserRole);
            
            // Get the payment
            Payment payment = firestore.collection(COLLECTION_NAME).document(paymentId).get().get().toObject(Payment.class);
            if (payment == null) {
                log.error("Payment not found with ID: {}", paymentId);
                throw new RuntimeException("Payment not found with ID: " + paymentId);
            }
            
            // Validate job order status
            if (jobOrderStatus == null || jobOrderStatus.isEmpty()) {
                log.error("Job order status cannot be empty for payment ID: {}", paymentId);
                throw new RuntimeException("Job order status cannot be empty");
            }
            
            // Role-based authorization checks using if-else
            if ("CUSTOMER".equalsIgnoreCase(currentUserRole)) {
                // Authorization check: ensure the current user is the customer who owns this payment
                if (!currentUserEmail.equals(payment.getCustomerEmail())) {
                    log.error("Unauthorized attempt to update payment. Current user: {}, Payment owner: {}", 
                        currentUserEmail, payment.getCustomerEmail());
                    throw new RuntimeException("You are not authorized to update this payment");
                }
                log.debug("Customer authorization passed for payment: {}", paymentId);
                
            } else if ("DRIVER".equalsIgnoreCase(currentUserRole)) {
                // Authorization check: ensure the current user is the driver assigned to this payment
                if (payment.getDriverId() == null || !currentUserEmail.equals(getDriverEmailById(payment.getDriverId()))) {
                    log.error("Unauthorized attempt to update payment. Current user: {}, Assigned driver: {}", 
                        currentUserEmail, payment.getDriverId());
                    throw new RuntimeException("You are not authorized to update this payment");
                }
                log.debug("Driver authorization passed for payment: {}", paymentId);
                
            } else {
                throw new RuntimeException("Invalid user role for updating job order status: " + currentUserRole);
            }
            
            // Store previous status for comparison
            String previousStatus = payment.getJobOrderStatus();
            log.debug("Previous job order status: {}", previousStatus);
            
            // Normalize the job order status to ensure consistent casing
            String normalizedStatus = normalizeStatus(jobOrderStatus);
            log.debug("Normalized new status: {}", normalizedStatus);
            
            // Update job order status
            payment.setJobOrderStatus(normalizedStatus);
            payment.setUpdatedAt(new Date());
            
            // If job is marked as Completed, release the truck (set to available)
            if ("Completed".equalsIgnoreCase(normalizedStatus) && payment.getTruckId() != null) {
                String truckId = payment.getTruckId();
                log.debug("Job marked as Completed, releasing truck: {}", truckId);
                
                Truck truck = firestore.collection("trucks").document(truckId).get().get().toObject(Truck.class);
                
                if (truck != null) {
                    // Update truck status to AVAILABLE
                    truck.setStatus("AVAILABLE");
                    truck.setUpdatedAt(new Date());
                    firestore.collection("trucks").document(truckId).set(truck);
                    log.debug("Updated truck status to AVAILABLE");
                }
                
                // Also update payment status
                payment.setStatus("COMPLETED");
            }
            
            // Save the updated payment
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);
            log.info("Successfully updated job order status to: {} for payment ID: {} by {} ({})", 
                normalizedStatus, paymentId, currentUserEmail, currentUserRole);
            
            // Send notification to the customer if the status changed to Accepted by driver
            log.debug("Checking if notification should be sent. New status: {}, Previous status: {}, Role: {}", 
                normalizedStatus, previousStatus, currentUserRole);
                
            if ("Accepted".equalsIgnoreCase(normalizedStatus) && 
                !normalizedStatus.equalsIgnoreCase(previousStatus) && 
                "DRIVER".equalsIgnoreCase(currentUserRole)) {
                log.info("Status changed to Accepted by driver, sending notification to customer");
                
                if (payment.getCustomerEmail() != null) {
                    log.debug("Looking up customer by email: {}", payment.getCustomerEmail());
                    User customer = userService.getUserByEmailOrUsername(payment.getCustomerEmail());
                    
                    if (customer != null) {
                        log.debug("Found customer: {}, userId: {}", customer.getEmail(), customer.getUserId());
                        
                        // Check if customer has a valid FCM token
                        boolean hasValidToken = notificationService.hasValidFcmToken(customer.getUserId());
                        if (!hasValidToken) {
                            log.warn("Customer does not have a valid FCM token, notification will not be sent");
                            return mapToResponseDTO(payment);
                        }
                        
                        Map<String, String> data = new HashMap<>();
                        data.put("paymentId", paymentId);
                        data.put("type", "JOB_ORDER_ACCEPTED");
                        
                        boolean notificationSent = notificationService.sendNotificationToUser(
                            customer.getUserId(),
                            "Job Order Accepted",
                            "Your job order has been accepted by the driver",
                            data
                        );
                        
                        log.info("Notification sent to customer: {}", notificationSent ? "success" : "failed");
                    } else {
                        log.warn("Customer not found for email: {}", payment.getCustomerEmail());
                    }
                } else {
                    log.warn("No customer email associated with payment: {}", paymentId);
                }
            } else {
                log.debug("No notification needed. Status: {}, Previous: {}, Role: {}", 
                    normalizedStatus, previousStatus, currentUserRole);
            }
            
            return mapToResponseDTO(payment);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error updating job order status for payment ID: {} by role", paymentId, e);
            throw new RuntimeException("Failed to update job order status: " + e.getMessage());
        }
    }
    
    /**
     * Get the current user's email from the security context
     * @return Current user's email or null if not found
     */
    private String getCurrentUserEmail() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else if (principal instanceof String) {
                return (String) principal;
            }
            return null;
        } catch (Exception e) {
            log.error("Error getting current user from security context", e);
            return null;
        }
    }
    
    /**
     * Get the current user's role from the security context
     * @return Current user's role or null if not found
     */
    private String getCurrentUserRole() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .findFirst()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .orElse(null);
        } catch (Exception e) {
            log.error("Error getting current user role from security context", e);
            return null;
        }
    }

    /**
     * Upload confirmation image for job status
     * Allows both customers and drivers to upload confirmation images
     * @param paymentId Payment ID
     * @param imageUrl Image URL or base64 data
     * @return Updated payment response
     */
    public PaymentResponseDTO uploadConfirmationImage(String paymentId, String imageUrl) {
        try {
            log.info("Uploading confirmation image for payment ID: {}", paymentId);
            
            // Get the current user from security context
            String currentUserEmail = getCurrentUserEmail();
            String currentUserRole = getCurrentUserRole();
            
            if (currentUserEmail == null || currentUserRole == null) {
                throw new RuntimeException("Unable to determine current user or role");
            }
            
            log.debug("Current user: {}, role: {}", currentUserEmail, currentUserRole);
            
            // Get the payment
            Payment payment = firestore.collection(COLLECTION_NAME).document(paymentId).get().get().toObject(Payment.class);
            if (payment == null) {
                log.error("Payment not found with ID: {}", paymentId);
                throw new RuntimeException("Payment not found with ID: " + paymentId);
            }
            
            // Validate image URL
            if (imageUrl == null || imageUrl.trim().isEmpty()) {
                throw new RuntimeException("Image URL cannot be empty");
            }
            
            // Role-based logic for storing the image
            if ("CUSTOMER".equalsIgnoreCase(currentUserRole)) {
                // Authorization check: ensure the current user is the customer who owns this payment
                if (!currentUserEmail.equals(payment.getCustomerEmail())) {
                    log.error("Unauthorized attempt to upload confirmation image. Current user: {}, Payment owner: {}", 
                        currentUserEmail, payment.getCustomerEmail());
                    throw new RuntimeException("You are not authorized to upload confirmation for this payment");
                }
                
                // Store in customerConfirmation field
                payment.setCustomerConfirmation(imageUrl);
                log.debug("Customer confirmation image uploaded for payment: {}", paymentId);
                
            } else if ("DRIVER".equalsIgnoreCase(currentUserRole)) {
                // Authorization check: ensure the current user is the driver assigned to this payment
                if (payment.getDriverId() == null || !currentUserEmail.equals(getDriverEmailById(payment.getDriverId()))) {
                    log.error("Unauthorized attempt to upload confirmation image. Current user: {}, Assigned driver: {}", 
                        currentUserEmail, payment.getDriverId());
                    throw new RuntimeException("You are not authorized to upload confirmation for this payment");
                }
                
                // Store in driverConfirmation field
                payment.setDriverConfirmation(imageUrl);
                log.debug("Driver confirmation image uploaded for payment: {}", paymentId);
                
            } else {
                throw new RuntimeException("Invalid user role for uploading confirmation image: " + currentUserRole);
            }
            
            // Update timestamp
            payment.setUpdatedAt(new Date());
            
            // Save the updated payment
            firestore.collection(COLLECTION_NAME).document(paymentId).set(payment);
            log.info("Successfully uploaded confirmation image for payment ID: {} by {} ({})", 
                paymentId, currentUserEmail, currentUserRole);
            
            return mapToResponseDTO(payment);
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error uploading confirmation image for payment ID: {}", paymentId, e);
            throw new RuntimeException("Failed to upload confirmation image: " + e.getMessage());
        }
    }
    
    /**
     * Helper method to get driver email by driver ID
     * @param driverId Driver ID
     * @return Driver email or null if not found
     */
    private String getDriverEmailById(String driverId) {
        try {
            User driver = userService.getUserById(driverId);
            return driver != null ? driver.getEmail() : null;
        } catch (Exception e) {
            log.error("Error getting driver email for ID: {}", driverId, e);
            return null;
        }
    }
}