package com.capstone.GrabTrash.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.capstone.GrabTrash.service.NotificationService;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/token")
    public ResponseEntity<?> updateFcmToken(
            @RequestParam String userId,
            @RequestParam String fcmToken) {
        try {
            notificationService.updateUserFcmToken(userId, fcmToken);
            Map<String, String> response = new HashMap<>();
            response.put("message", "FCM token updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update FCM token: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/token")
    public ResponseEntity<?> removeFcmToken(@RequestParam String userId) {
        try {
            notificationService.removeUserFcmToken(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "FCM token removed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to remove FCM token: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
} 