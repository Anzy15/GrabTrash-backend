package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.model.PrivateEntity;
import com.capstone.GrabTrash.model.User;
import com.capstone.GrabTrash.dto.PrivateEntityUpdateRequest;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class PrivateEntityService {
    private final Firestore firestore;
    private final UserService userService;

    @Autowired
    public PrivateEntityService(Firestore firestore, UserService userService) {
        this.firestore = firestore;
        this.userService = userService;
    }

    /**
     * Update private entity information
     */
    public ResponseEntity<?> updatePrivateEntity(String userId, PrivateEntityUpdateRequest request) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String userEmail = authentication.getName();
            User currentUser = userService.getUserByEmailOrUsername(userEmail);
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Check permissions
            boolean isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());
            boolean isPrivateEntity = "private_entity".equalsIgnoreCase(currentUser.getRole());
            boolean isUpdatingSelf = currentUser.getUserId().equals(userId);

            if (!isAdmin && !(isPrivateEntity && isUpdatingSelf)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. You can only update your own information.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Find existing entity for this user
            Query query = firestore.collection("private_entities").whereEqualTo("userId", userId);
            QuerySnapshot querySnapshot = query.get().get();
            
            PrivateEntity entity;
            String entityId;
            
            if (!querySnapshot.isEmpty()) {
                // Update existing entity
                DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                entity = doc.toObject(PrivateEntity.class);
                entityId = entity.getEntityId();
            } else {
                // Create new entity
                entityId = UUID.randomUUID().toString();
                entity = new PrivateEntity();
                entity.setEntityId(entityId);
                entity.setUserId(userId);
            }

            // Update fields if provided
            if (request.getEntityName() != null) entity.setEntityName(request.getEntityName());
            if (request.getLatitude() != null) entity.setLatitude(request.getLatitude());
            if (request.getLongitude() != null) entity.setLongitude(request.getLongitude());
            if (request.getEntityWasteType() != null) entity.setEntityWasteType(request.getEntityWasteType());
            if (request.getAddress() != null) entity.setAddress(request.getAddress());
            if (request.getEntityStatus() != null) entity.setEntityStatus(request.getEntityStatus());

            // Save to Firestore using entityId as document ID
            firestore.collection("private_entities").document(entityId).set(entity).get();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Private entity information updated successfully");
            response.put("entityId", entityId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get private entity information by userId
     */
    public ResponseEntity<?> getPrivateEntity(String userId) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String userEmail = authentication.getName();
            User currentUser = userService.getUserByEmailOrUsername(userEmail);
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Check permissions
            boolean isAdmin = "admin".equalsIgnoreCase(currentUser.getRole());
            boolean isPrivateEntity = "private_entity".equalsIgnoreCase(currentUser.getRole());
            boolean isRequestingSelf = currentUser.getUserId().equals(userId);

            if (!isAdmin && !(isPrivateEntity && isRequestingSelf)) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. You can only view your own information.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Query for entity by userId
            Query query = firestore.collection("private_entities").whereEqualTo("userId", userId);
            QuerySnapshot querySnapshot = query.get().get();
            
            if (querySnapshot.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            PrivateEntity entity = querySnapshot.getDocuments().get(0).toObject(PrivateEntity.class);
            return ResponseEntity.ok(entity);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get all private entities (admin only)
     */
    public ResponseEntity<?> getAllPrivateEntities() {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

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

            // Get all private entities
            QuerySnapshot querySnapshot = firestore.collection("private_entities").get().get();
            List<PrivateEntity> entities = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                PrivateEntity entity = document.toObject(PrivateEntity.class);
                entities.add(entity);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("entities", entities);
            response.put("count", entities.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 