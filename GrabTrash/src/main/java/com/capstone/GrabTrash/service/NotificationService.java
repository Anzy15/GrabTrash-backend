package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.model.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.api.core.ApiFuture;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final Firestore firestore;
    private final FirebaseMessaging firebaseMessaging;

    @Autowired
    public NotificationService(Firestore firestore, FirebaseMessaging firebaseMessaging) {
        this.firestore = firestore;
        this.firebaseMessaging = firebaseMessaging;
        log.info("NotificationService initialized with FirebaseMessaging instance");
    }

    /**
     * Register or update FCM token for a user
     */
    public ResponseEntity<?> registerFcmToken(String userId, String fcmToken) {
        try {
            log.info("Registering FCM token for user: {}", userId);
            
            if (fcmToken == null || fcmToken.isEmpty()) {
                log.error("FCM token cannot be null or empty");
                Map<String, String> error = new HashMap<>();
                error.put("error", "FCM token cannot be null or empty");
                return ResponseEntity.badRequest().body(error);
            }
            
            Map<String, Object> updates = new HashMap<>();
            updates.put("fcmToken", fcmToken);

            firestore.collection("users").document(userId).update(updates).get();
            log.info("FCM token registered successfully for user: {}", userId);

            Map<String, String> response = new HashMap<>();
            response.put("message", "FCM token registered successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error registering FCM token for user {}: {}", userId, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to register FCM token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Validates and sanitizes an FCM token
     * Common issues:
     * 1. Trailing or leading whitespace
     * 2. Quotes or other characters that shouldn't be part of the token
     * 3. Malformed token formats
     */
    public String validateAndSanitizeToken(String token) {
        if (token == null) {
            return null;
        }
        
        // Remove any whitespace, quotes, or other common invalid characters
        String sanitized = token.trim()
                .replace("\"", "")
                .replace("'", "");
                
        // Check if token has the expected structure (usually contains a colon)
        if (!sanitized.contains(":")) {
            log.warn("FCM token doesn't have the expected format (missing colon): {}", sanitized);
        }
        
        return sanitized;
    }

    /**
     * Send notification to a specific user by FCM token
     */
    public String sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            log.info("Sending notification to token: {}", fcmToken);
            
            // Sanitize and validate the token
            fcmToken = validateAndSanitizeToken(fcmToken);
            
            if (fcmToken == null || fcmToken.isEmpty()) {
                log.error("FCM token cannot be null or empty");
                return null;
            }
            
            // Validate token format - basic check
            if (!fcmToken.contains(":")) {
                log.warn("FCM token format appears invalid: {}", fcmToken);
                // Continue anyway as Firebase will validate it
            }
            
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build();
            
            // Configure Android specific options
            AndroidNotification androidNotification = AndroidNotification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .setPriority(AndroidNotification.Priority.HIGH)
                    .setDefaultSound(true)
                    .setDefaultVibrateTimings(true)
                    .build();
            
            AndroidConfig androidConfig = AndroidConfig.builder()
                    .setPriority(AndroidConfig.Priority.HIGH)
                    .setNotification(androidNotification)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification)
                    .setAndroidConfig(androidConfig);

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }
            
            Message message = messageBuilder.build();
            log.debug("Message built: {}", message);

            try {
                String response = firebaseMessaging.send(message);
                log.info("Successfully sent notification: {}", response);
                return response;
            } catch (FirebaseMessagingException e) {
                if (e.getMessage().contains("not a valid FCM registration token")) {
                    log.error("Invalid FCM token format: {}", fcmToken);
                } else if (e.getMessage().contains("registration-token-not-registered")) {
                    log.error("FCM token is no longer valid (app uninstalled or token expired): {}", fcmToken);
                } else {
                    log.error("Firebase messaging error: {}", e.getMessage());
                }
                log.error("Failed to send notification to token: {}, error: {}", fcmToken, e.getMessage(), e);
                return null;
            }
        } catch (Exception e) {
            log.error("Unexpected error when sending notification: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Send notification to users with specific roles
     */
    public int sendNotificationToRole(String role, String title, String body, Map<String, String> data) {
        try {
            log.info("Sending notification to users with role: {}", role);
            
            if (role == null || role.isEmpty()) {
                log.error("Role cannot be null or empty");
                return 0;
            }
            
            ApiFuture<QuerySnapshot> future = firestore.collection("users")
                    .whereEqualTo("role", role)
                    .get();

            List<String> tokens = new ArrayList<>();
            try {
                QuerySnapshot snapshot = future.get(); // This can throw InterruptedException and ExecutionException
                for (User user : snapshot.toObjects(User.class)) {
                    if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                        log.debug("Adding token for user: {}", user.getUserId());
                        tokens.add(validateAndSanitizeToken(user.getFcmToken()));
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error retrieving user tokens: {}", e.getMessage(), e);
                return 0;
            }

            if (tokens.isEmpty()) {
                log.warn("No valid FCM tokens found for users with role: {}", role);
                return 0;
            }
            
            // Remove any null tokens that may have been introduced during validation
            tokens.removeIf(token -> token == null || token.isEmpty());
            
            if (tokens.isEmpty()) {
                log.warn("No valid FCM tokens left after validation for role: {}", role);
                return 0;
            }
            
            log.info("Found {} tokens for users with role: {}", tokens.size(), role);
            return sendMulticastNotification(tokens, title, body, data);
        } catch (Exception e) {
            log.error("Failed to send notifications to users with role: {}", role, e);
            return 0;
        }
    }

    /**
     * Send notification to users in a specific barangay
     */
    public int sendNotificationToBarangay(String barangayId, String title, String body, Map<String, String> data) {
        try {
            log.info("Sending notification to users in barangay: {}", barangayId);
            
            if (barangayId == null || barangayId.isEmpty()) {
                log.error("BarangayId cannot be null or empty");
                return 0;
            }
            
            ApiFuture<QuerySnapshot> future = firestore.collection("users")
                    .whereEqualTo("barangayId", barangayId)
                    .whereEqualTo("role", "customer")
                    .get();

            List<String> tokens = new ArrayList<>();
            try {
                QuerySnapshot snapshot = future.get(); // This can throw InterruptedException and ExecutionException
                for (User user : snapshot.toObjects(User.class)) {
                    if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                        log.debug("Found token for user: {}", user.getUserId());
                        tokens.add(validateAndSanitizeToken(user.getFcmToken()));
                    } else {
                        log.debug("No token for user: {}", user.getUserId());
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error retrieving user tokens: {}", e.getMessage(), e);
                return 0;
            }

            if (tokens.isEmpty()) {
                log.warn("No valid FCM tokens found for users in barangay: {}", barangayId);
                return 0;
            }
            
            log.info("Found {} tokens for users in barangay: {}", tokens.size(), barangayId);
            
            // Remove any null tokens that may have been introduced during validation
            tokens.removeIf(token -> token == null || token.isEmpty());
            
            if (tokens.isEmpty()) {
                log.warn("No valid FCM tokens left after validation for barangay: {}", barangayId);
                return 0;
            }
            
            return sendMulticastNotification(tokens, title, body, data);
        } catch (Exception e) {
            log.error("Unexpected error when sending notifications to barangay {}: {}", barangayId, e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Send notification to multiple devices using their FCM tokens
     */
    private int sendMulticastNotification(List<String> tokens, String title, String body, Map<String, String> data) {
        int maxRetries = 3;
        int successCount = 0;
        
        log.info("Sending notifications to {} devices individually (avoiding batch API)", tokens.size());
        
        // Send to each token individually instead of using batch API
        for (String token : tokens) {
            int retryCount = 0;
            boolean success = false;
            
            while (retryCount < maxRetries && !success) {
                try {
                    log.debug("Sending to token: {} (attempt {})", token, retryCount + 1);
                    
                    // Sanitize token
                    token = validateAndSanitizeToken(token);
                    if (token == null || token.isEmpty()) {
                        log.warn("Invalid token, skipping");
                        break;
                    }
                    
                    String response = sendNotification(token, title, body, data);
                    if (response != null) {
                        success = true;
                        successCount++;
                        log.debug("Successfully sent notification to token: {}", token);
                    } else {
                        log.warn("Failed to send notification to token: {}, retrying...", token);
                        retryCount++;
                        if (retryCount < maxRetries) {
                            // Add delay between retries
                            Thread.sleep(1000);
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error sending notification to token: {}, error: {}", token, e.getMessage());
                    retryCount++;
                    if (retryCount < maxRetries) {
                        try {
                            // Add delay between retries
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
            }
        }
        
        log.info("Completed sending individual notifications. Success count: {}/{}", successCount, tokens.size());
        return successCount;
    }

    /**
     * Send notification to a specific user by userId
     */
    public boolean sendNotificationToUser(String userId, String title, String body, Map<String, String> data) {
        try {
            log.info("Sending notification to user: {}", userId);
            
            User user = firestore.collection("users").document(userId).get().get().toObject(User.class);
            
            if (user == null) {
                log.warn("User not found: {}", userId);
                return false;
            }
            
            if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
                log.warn("User has no FCM token: {}", userId);
                return false;
            }
            
            log.debug("Found FCM token for user: {}", userId);
            String result = sendNotification(user.getFcmToken(), title, body, data);
            return result != null;
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send notification to user: {}", userId, e);
            return false;
        }
    }
} 