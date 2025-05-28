package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.model.User;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.MulticastMessage;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.api.core.ApiFuture;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class NotificationService {

    private final Firestore firestore;
    private final FirebaseMessaging firebaseMessaging;

    @Autowired
    public NotificationService(Firestore firestore) {
        this.firestore = firestore;
        this.firebaseMessaging = FirebaseMessaging.getInstance();
    }

    /**
     * Register or update FCM token for a user
     */
    public ResponseEntity<?> registerFcmToken(String userId, String fcmToken) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("fcmToken", fcmToken);

            firestore.collection("users").document(userId).update(updates).get();

            Map<String, String> response = new HashMap<>();
            response.put("message", "FCM token registered successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error registering FCM token", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to register FCM token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Send notification to a specific user
     */
    public boolean sendNotification(String fcmToken, String title, String body, Map<String, String> data) {
        try {
            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            String response = firebaseMessaging.send(messageBuilder.build());
            log.info("Successfully sent notification: {}", response);
            return true;
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send notification", e);
            return false;
        }
    }

    /**
     * Send notification to users with specific roles
     */
    public int sendNotificationToRole(String role, String title, String body, Map<String, String> data) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("users")
                    .whereEqualTo("role", role)
                    .get();

            List<String> tokens = new ArrayList<>();
            for (User user : future.get().toObjects(User.class)) {
                if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                    tokens.add(user.getFcmToken());
                }
            }

            if (tokens.isEmpty()) {
                log.warn("No valid FCM tokens found for users with role: {}", role);
                return 0;
            }

            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            int successCount = firebaseMessaging.sendMulticast(messageBuilder.build()).getSuccessCount();
            log.info("Successfully sent {} notifications to users with role: {}", successCount, role);
            return successCount;
        } catch (FirebaseMessagingException | InterruptedException | ExecutionException e) {
            log.error("Failed to send notifications to users with role: {}", role, e);
            return 0;
        }
    }

    /**
     * Send notification to users in a specific barangay
     */
    public int sendNotificationToBarangay(String barangayId, String title, String body, Map<String, String> data) {
        try {
            ApiFuture<QuerySnapshot> future = firestore.collection("users")
                    .whereEqualTo("barangayId", barangayId)
                    .whereEqualTo("role", "customer")
                    .get();

            List<String> tokens = new ArrayList<>();
            for (User user : future.get().toObjects(User.class)) {
                if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
                    tokens.add(user.getFcmToken());
                }
            }

            if (tokens.isEmpty()) {
                log.warn("No valid FCM tokens found for users in barangay: {}", barangayId);
                return 0;
            }

            MulticastMessage.Builder messageBuilder = MulticastMessage.builder()
                    .addAllTokens(tokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build());

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            int successCount = firebaseMessaging.sendMulticast(messageBuilder.build()).getSuccessCount();
            log.info("Successfully sent {} notifications to users in barangay: {}", successCount, barangayId);
            return successCount;
        } catch (FirebaseMessagingException | InterruptedException | ExecutionException e) {
            log.error("Failed to send notifications to users in barangay: {}", barangayId, e);
            return 0;
        }
    }

    /**
     * Send notification to a specific user by userId
     */
    public boolean sendNotificationToUser(String userId, String title, String body, Map<String, String> data) {
        try {
            User user = firestore.collection("users").document(userId).get().get().toObject(User.class);
            
            if (user == null || user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
                log.warn("User not found or has no FCM token: {}", userId);
                return false;
            }
            
            return sendNotification(user.getFcmToken(), title, body, data);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Failed to send notification to user: {}", userId, e);
            return false;
        }
    }
} 