package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.dto.FcmTokenRequest;
import com.capstone.GrabTrash.dto.NotificationRequest;
import com.capstone.GrabTrash.service.NotificationService;
import com.capstone.GrabTrash.service.UserService;
import com.capstone.GrabTrash.service.CollectionScheduleService;
import com.capstone.GrabTrash.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationService notificationService;
    private final UserService userService;
    private final CollectionScheduleService collectionScheduleService;

    @Autowired
    public NotificationController(
            NotificationService notificationService, 
            UserService userService,
            CollectionScheduleService collectionScheduleService) {
        this.notificationService = notificationService;
        this.userService = userService;
        this.collectionScheduleService = collectionScheduleService;
    }

    @PostMapping("/register-token")
    public ResponseEntity<?> registerToken(@RequestBody FcmTokenRequest request) {
        try {
            // Get authentication details
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName(); // This is the email from JWT
            
            log.info("Registering FCM token for user email: {}", email);
            
            // Find the user by email to get the actual userId
            User user = userService.getUserByEmailOrUsername(email);
            if (user == null || user.getUserId() == null) {
                log.error("User not found for email: {}", email);
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found for email: " + email);
                return ResponseEntity.badRequest().body(error);
            }
            
            log.info("Found user: {}, userId: {}", user.getEmail(), user.getUserId());
            
            // Now use the actual userId to register the token
            return notificationService.registerFcmToken(user.getUserId(), request.getFcmToken());
        } catch (Exception e) {
            log.error("Failed to register token", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process request: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Direct notification endpoint using NotificationRequest DTO
    @PostMapping("/send")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) {
        try {
            log.info("Sending notification to token: {}", request.getFcmToken());
            
            Map<String, String> data = new HashMap<>();
            if (request.getData() != null) {
                if (request.getData() instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> dataMap = (Map<String, Object>) request.getData();
                    for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                        data.put(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
            
            String response = notificationService.sendNotification(
                request.getFcmToken(),
                request.getTitle(),
                request.getBody(),
                data
            );
            
            if (response != null) {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("messageId", response);
                responseMap.put("status", "success");
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to send notification"
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Error: " + e.getMessage()
            ));
        }
    }
    
    // Flexible endpoint that accepts raw JSON with either token or fcmToken
    @PostMapping("/send-flexible")
    public ResponseEntity<?> sendFlexibleNotification(@RequestBody Map<String, Object> requestMap) {
        try {
            // Extract token from either "token" or "fcmToken" field
            String token = null;
            if (requestMap.containsKey("fcmToken")) {
                token = (String) requestMap.get("fcmToken");
                log.info("Using fcmToken from request");
            } else if (requestMap.containsKey("token")) {
                token = (String) requestMap.get("token");
                log.info("Using token from request");
            }
            
            // Sanitize token
            token = notificationService.validateAndSanitizeToken(token);
            
            if (token == null || token.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Missing required token field (either 'token' or 'fcmToken' must be provided)"
                ));
            }
            
            String title = (String) requestMap.get("title");
            String body = (String) requestMap.get("body");
            Object dataObj = requestMap.get("data");
            
            log.info("Sending notification to token: {}", token);
            
            if (title == null || body == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Missing required fields (title, body)"
                ));
            }
            
            // Convert data object to string map if present
            Map<String, String> data = new HashMap<>();
            if (dataObj != null && dataObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    if (entry.getValue() != null) {
                        data.put(entry.getKey(), entry.getValue().toString());
                    }
                }
            }
            
            String response = notificationService.sendNotification(token, title, body, data);
            
            if (response != null) {
                Map<String, String> responseMap = new HashMap<>();
                responseMap.put("messageId", response);
                responseMap.put("status", "success");
                return ResponseEntity.ok(responseMap);
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Failed to send notification"
                ));
            }
        } catch (Exception e) {
            log.error("Failed to send notification", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Error: " + e.getMessage()
            ));
        }
    }
    
    // Compatibility endpoint that accepts a token field instead of fcmToken
    @PostMapping("/send-compat")
    public ResponseEntity<?> sendNotificationCompat(@RequestBody Map<String, Object> request) {
        try {
            String token = (String) request.get("token");
            String title = (String) request.get("title");
            String body = (String) request.get("body");
            Object data = request.get("data");
            
            log.info("Sending notification using compatibility endpoint to token: {}", token);
            
            if (token == null || title == null || body == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Missing required fields (token, title, body)"
                ));
            }
            
            // Create a proper notification request
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setFcmToken(token);
            notificationRequest.setTitle(title);
            notificationRequest.setBody(body);
            notificationRequest.setData(data);
            
            // Forward to the main endpoint
            return sendNotification(notificationRequest);
        } catch (Exception e) {
            log.error("Failed to send notification via compatibility endpoint", e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Error: " + e.getMessage()
            ));
        }
    }

    // For testing purposes only - should be secured properly in production
    @PostMapping("/test-send")
    public ResponseEntity<?> testSendNotification(
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String body) {
        
        log.info("Testing notification to userId: {}", userId);
        boolean success = notificationService.sendNotificationToUser(userId, title, body, null);
        
        if (success) {
            return ResponseEntity.ok().body("Notification sent successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to send notification");
        }
    }
    
    // For testing notifications to a barangay
    @PostMapping("/test-barangay")
    public ResponseEntity<?> testBarangayNotification(
            @RequestParam String barangayId,
            @RequestParam String title,
            @RequestParam String body) {
        
        log.info("Testing notification to barangay: {}", barangayId);
        Map<String, String> data = new HashMap<>();
        data.put("type", "TEST_NOTIFICATION");
        
        int sentCount = notificationService.sendNotificationToBarangay(barangayId, title, body, data);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", sentCount > 0);
        response.put("sentCount", sentCount);
        response.put("barangayId", barangayId);
        
        return ResponseEntity.ok(response);
    }
    
    // For testing notifications to users with a specific role
    @PostMapping("/test-role")
    public ResponseEntity<?> testRoleNotification(
            @RequestParam String role,
            @RequestParam String title,
            @RequestParam String body) {
        
        log.info("Testing notification to role: {}", role);
        Map<String, String> data = new HashMap<>();
        data.put("type", "TEST_NOTIFICATION");
        
        int sentCount = notificationService.sendNotificationToRole(role, title, body, data);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", sentCount > 0);
        response.put("sentCount", sentCount);
        response.put("role", role);
        
        return ResponseEntity.ok(response);
    }
    
    // For testing the daily collection reminders
    @PostMapping("/test-collection-reminders")
    public ResponseEntity<?> testCollectionReminders() {
        try {
            log.info("Testing collection reminders");
            collectionScheduleService.sendTodayCollectionReminders();
            return ResponseEntity.ok().body("Collection reminders test triggered successfully");
        } catch (Exception e) {
            log.error("Failed to trigger collection reminders", e);
            return ResponseEntity.badRequest().body("Failed to trigger collection reminders: " + e.getMessage());
        }
    }
    
    // Test endpoint to check if a token is valid
    @PostMapping("/test-token-validity")
    public ResponseEntity<?> testTokenValidity(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Token is required"
            ));
        }
        
        log.info("Testing token validity: {}", token);
        
        try {
            // Send a simple test message
            String response = notificationService.sendNotification(
                token,
                "Token Validation Test",
                "This is a test to validate your FCM token",
                Map.of("test", "true")
            );
            
            if (response != null) {
                return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Token is valid",
                    "messageId", response
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Token validation failed"
                ));
            }
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "status", "error",
                "message", "Error: " + e.getMessage()
            ));
        }
    }
} 