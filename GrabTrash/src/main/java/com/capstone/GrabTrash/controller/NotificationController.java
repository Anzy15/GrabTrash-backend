package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.dto.FcmTokenRequest;
import com.capstone.GrabTrash.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/register-token")
    public ResponseEntity<?> registerToken(@RequestBody FcmTokenRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        return notificationService.registerFcmToken(userId, request.getFcmToken());
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