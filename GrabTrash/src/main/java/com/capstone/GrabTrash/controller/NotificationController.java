package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.dto.FcmTokenRequest;
import com.capstone.GrabTrash.service.NotificationService;
import com.capstone.GrabTrash.service.UserService;
import com.capstone.GrabTrash.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    @Autowired
    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    @PostMapping("/register-token")
    public ResponseEntity<?> registerToken(@RequestBody FcmTokenRequest request) {
        try {
            // Get authentication details
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName(); // This is the email from JWT
            
            // Find the user by email to get the actual userId
            User user = userService.getUserByEmailOrUsername(email);
            if (user == null || user.getUserId() == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found for email: " + email);
                return ResponseEntity.badRequest().body(error);
            }
            
            // Now use the actual userId to register the token
            return notificationService.registerFcmToken(user.getUserId(), request.getFcmToken());
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to process request: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // For testing purposes only - should be secured properly in production
    @PostMapping("/test-send")
    public ResponseEntity<?> testSendNotification(
            @RequestParam String userId,
            @RequestParam String title,
            @RequestParam String body) {
        
        boolean success = notificationService.sendNotificationToUser(userId, title, body, null);
        
        if (success) {
            return ResponseEntity.ok().body("Notification sent successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to send notification");
        }
    }
} 