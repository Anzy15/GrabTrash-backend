package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.model.PickupRequest;
import com.capstone.GrabTrash.dto.PickupRequestDTO;
import com.google.cloud.firestore.*;
import com.google.cloud.Timestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class PickupRequestService {
    private final Firestore firestore;
    private final UserService userService;

    @Autowired
    public PickupRequestService(Firestore firestore, UserService userService) {
        this.firestore = firestore;
        this.userService = userService;
    }

    /**
     * Create a new pickup request
     */
    public ResponseEntity<?> createPickupRequest(PickupRequestDTO requestDTO) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String userEmail = authentication.getName();
            com.capstone.GrabTrash.model.User user = userService.getUserByEmailOrUsername(userEmail);
            
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Create a new pickup request
            PickupRequest pickupRequest = new PickupRequest();
            pickupRequest.setUserId(user.getUserId());
            pickupRequest.setStatus("PENDING");
            pickupRequest.setLocation(requestDTO.getLocation());
            pickupRequest.setAddress(requestDTO.getAddress());
            pickupRequest.setDescription(requestDTO.getDescription());
            pickupRequest.setTrashType(requestDTO.getTrashType());
            pickupRequest.setCreatedAt(Timestamp.now());
            pickupRequest.setUpdatedAt(Timestamp.now());

            // Save to Firestore
            String requestId = firestore.collection("pickup_requests").document().getId();
            pickupRequest.setRequestId(requestId);
            firestore.collection("pickup_requests").document(requestId).set(pickupRequest).get();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Pickup request created successfully");
            response.put("requestId", requestId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get all pickup requests for the current user
     */
    public ResponseEntity<?> getUserPickupRequests() {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String userEmail = authentication.getName();
            com.capstone.GrabTrash.model.User user = userService.getUserByEmailOrUsername(userEmail);
            
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Get all pickup requests for the user
            Query query = firestore.collection("pickup_requests")
                .whereEqualTo("userId", user.getUserId());
            QuerySnapshot querySnapshot = query.get().get();
            
            List<PickupRequest> requests = new ArrayList<>();
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                requests.add(document.toObject(PickupRequest.class));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("requests", requests);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get a specific pickup request by ID
     */
    public ResponseEntity<?> getPickupRequest(String requestId) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String userEmail = authentication.getName();
            com.capstone.GrabTrash.model.User user = userService.getUserByEmailOrUsername(userEmail);
            
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Get the pickup request
            DocumentSnapshot document = firestore.collection("pickup_requests").document(requestId).get().get();
            
            if (!document.exists()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Pickup request not found");
                return ResponseEntity.notFound().build();
            }

            PickupRequest pickupRequest = document.toObject(PickupRequest.class);
            
            // Check if the user is authorized to view this request
            if (!pickupRequest.getUserId().equals(user.getUserId()) && !"admin".equalsIgnoreCase(user.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            return ResponseEntity.ok(pickupRequest);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Update the status of a pickup request
     */
    public ResponseEntity<?> updatePickupRequestStatus(String requestId, String status, Double trashWeight) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String userEmail = authentication.getName();
            com.capstone.GrabTrash.model.User user = userService.getUserByEmailOrUsername(userEmail);
            
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if the user is an admin
            if (!"admin".equalsIgnoreCase(user.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Get the pickup request
            DocumentSnapshot document = firestore.collection("pickup_requests").document(requestId).get().get();
            
            if (!document.exists()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Pickup request not found");
                return ResponseEntity.notFound().build();
            }

            PickupRequest pickupRequest = document.toObject(PickupRequest.class);
            
            // Update the status
            pickupRequest.setStatus(status);
            pickupRequest.setUpdatedAt(Timestamp.now());
            
            // If the status is COMPLETED, set the completedAt timestamp and trash weight
            if ("COMPLETED".equals(status)) {
                pickupRequest.setCompletedAt(Timestamp.now());
                pickupRequest.setTrashWeight(trashWeight);
            }

            // Save to Firestore
            firestore.collection("pickup_requests").document(requestId).set(pickupRequest).get();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Pickup request status updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get total trash picked up (admin only)
     */
    public ResponseEntity<?> getTotalTrashPickedUp() {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String userEmail = authentication.getName();
            com.capstone.GrabTrash.model.User user = userService.getUserByEmailOrUsername(userEmail);
            
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if the user is an admin
            if (!"admin".equalsIgnoreCase(user.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Get all completed pickup requests
            Query query = firestore.collection("pickup_requests")
                .whereEqualTo("status", "COMPLETED");
            QuerySnapshot querySnapshot = query.get().get();
            
            double totalWeight = 0.0;
            int totalRequests = 0;
            
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                PickupRequest pickupRequest = document.toObject(PickupRequest.class);
                if (pickupRequest.getTrashWeight() != null) {
                    totalWeight += pickupRequest.getTrashWeight();
                }
                totalRequests++;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("totalTrashPickedUp", totalWeight);
            response.put("totalCompletedRequests", totalRequests);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 