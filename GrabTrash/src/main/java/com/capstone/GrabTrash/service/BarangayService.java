package com.capstone.GrabTrash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.capstone.GrabTrash.model.Barangay;
import com.capstone.GrabTrash.model.User;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.Timestamp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class BarangayService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private UserService userService;

    private String generateShortId() {
        // Get current timestamp in milliseconds
        long timestamp = System.currentTimeMillis();
        // Convert to base36 (0-9, a-z) to make it shorter
        String timestampStr = Long.toString(timestamp, 36);
        
        // Generate 3 random characters
        Random random = new Random();
        String randomChars = "";
        for (int i = 0; i < 3; i++) {
            // Generate random character between a-z
            char c = (char) (random.nextInt(26) + 'a');
            randomChars += c;
        }
        
        // Combine timestamp and random chars
        return timestampStr.substring(timestampStr.length() - 4) + randomChars;
    }

    public ResponseEntity<?> addBarangay(Barangay barangay) {
        try {
            // Get the current authenticated user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Get the current user's email
            String userEmail = authentication.getName();
            User currentUser = userService.getUserByEmailOrUsername(userEmail);
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if the user is an admin
            if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Generate short ID
            barangay.setBarangayId(generateShortId());
            barangay.setCreatedAt(Timestamp.now());

            // Save to Firestore
            firestore.collection("barangays").document(barangay.getBarangayId()).set(barangay);

            return ResponseEntity.ok(barangay);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add barangay: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> removeBarangay(String barangayId) {
        try {
            // Get the current authenticated user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Get the current user's email
            String userEmail = authentication.getName();
            User currentUser = userService.getUserByEmailOrUsername(userEmail);
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if the user is an admin
            if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Check if barangay exists
            Barangay barangay = firestore.collection("barangays").document(barangayId).get().get().toObject(Barangay.class);
            if (barangay == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Barangay not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Soft delete by setting isActive to false
            barangay.setActive(false);
            firestore.collection("barangays").document(barangayId).set(barangay);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Barangay removed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to remove barangay: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> getAllBarangays() {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection("barangays")
                    .whereEqualTo("isActive", true)
                    .get()
                    .get()
                    .getDocuments();

            List<Barangay> barangays = documents.stream()
                    .map(doc -> doc.toObject(Barangay.class))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(barangays);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get barangays: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> getBarangayById(String barangayId) {
        try {
            Barangay barangay = firestore.collection("barangays").document(barangayId).get().get().toObject(Barangay.class);
            if (barangay == null || !barangay.isActive()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Barangay not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            return ResponseEntity.ok(barangay);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get barangay: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> reactivateBarangay(String barangayId) {
        try {
            // Get the current authenticated user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Get the current user's email
            String userEmail = authentication.getName();
            User currentUser = userService.getUserByEmailOrUsername(userEmail);
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if the user is an admin
            if (!"admin".equalsIgnoreCase(currentUser.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Check if barangay exists
            Barangay barangay = firestore.collection("barangays").document(barangayId).get().get().toObject(Barangay.class);
            if (barangay == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Barangay not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Reactivate by setting isActive to true
            barangay.setActive(true);
            firestore.collection("barangays").document(barangayId).set(barangay);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Barangay reactivated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reactivate barangay: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 