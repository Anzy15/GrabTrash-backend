package com.capstone.GrabTrash.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.MulticastMessage;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.DocumentSnapshot;
import com.capstone.GrabTrash.model.User;
import com.capstone.GrabTrash.model.CollectionSchedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NotificationService {
    private final FirebaseMessaging firebaseMessaging;
    private final Firestore firestore;
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    public NotificationService(FirebaseMessaging firebaseMessaging, Firestore firestore) {
        this.firebaseMessaging = firebaseMessaging;
        this.firestore = firestore;
    }

    public void updateUserFcmToken(String userId, String fcmToken) {
        try {
            // Update user's FCM token in Firestore
            firestore.collection("users").document(userId).update("fcmToken", fcmToken).get();
        } catch (Exception e) {
            System.err.println("Error updating FCM token: " + e.getMessage());
        }
    }

    public void removeUserFcmToken(String userId) {
        try {
            // Remove user's FCM token from Firestore
            firestore.collection("users").document(userId).update("fcmToken", null).get();
        } catch (Exception e) {
            System.err.println("Error removing FCM token: " + e.getMessage());
        }
    }

    public void sendNotificationToBarangay(String barangayId, String title, String body, Map<String, String> data) {
        try {
            // Get all users from the specified barangay
            QuerySnapshot querySnapshot = firestore.collection("users")
                    .whereEqualTo("barangayId", barangayId)
                    .whereNotEqualTo("fcmToken", null)
                    .get()
                    .get();

            List<String> tokens = new ArrayList<>();
            for (var doc : querySnapshot.getDocuments()) {
                User user = doc.toObject(User.class);
                if (user.getFcmToken() != null) {
                    tokens.add(user.getFcmToken());
                }
            }

            if (!tokens.isEmpty()) {
                MulticastMessage message = MulticastMessage.builder()
                        .setNotification(Notification.builder()
                                .setTitle(title)
                                .setBody(body)
                                .build())
                        .putAllData(data)
                        .addAllTokens(tokens)
                        .build();

                BatchResponse response = firebaseMessaging.sendMulticast(message);
                System.out.println("Successfully sent message to " + response.getSuccessCount() + " devices");
            }
        } catch (Exception e) {
            System.err.println("Error sending notifications: " + e.getMessage());
        }
    }

    public void notifyScheduleChange(CollectionSchedule schedule, String changeType) {
        String title;
        String body;
        Map<String, String> data = new HashMap<>();
        data.put("scheduleId", schedule.getScheduleId());
        data.put("type", "schedule_change");
        data.put("changeType", changeType);

        switch (changeType) {
            case "new":
                title = "New Collection Schedule";
                body = String.format("New garbage collection schedule for %s: %s waste collection", 
                    schedule.getBarangayName(), schedule.getWasteType());
                break;
            case "update":
                title = "Schedule Update";
                body = String.format("Collection schedule updated for %s: %s waste collection", 
                    schedule.getBarangayName(), schedule.getWasteType());
                break;
            case "cancel":
                title = "Schedule Cancelled";
                body = String.format("Collection schedule cancelled for %s: %s waste collection", 
                    schedule.getBarangayName(), schedule.getWasteType());
                break;
            case "reminder":
                title = "Collection Reminder";
                body = String.format("Reminder: %s waste collection scheduled for your area tomorrow", 
                    schedule.getWasteType());
                break;
            case "today":
                title = "Today's Collection";
                body = String.format("Today's %s waste collection will be conducted in your area", 
                    schedule.getWasteType());
                break;
            default:
                return;
        }

        sendNotificationToBarangay(schedule.getBarangayId(), title, body, data);
    }

    public void sendScheduleReminders() {
        try {
            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();

            // Set up tomorrow's date range
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date tomorrow = calendar.getTime();

            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date dayAfterTomorrow = calendar.getTime();

            // Set up today's date range
            calendar.setTime(now);
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            Date startOfToday = calendar.getTime();

            calendar.add(Calendar.DAY_OF_YEAR, 1);
            Date endOfToday = calendar.getTime();

            // Query for schedules happening tomorrow
            QuerySnapshot tomorrowSchedules = firestore.collection("collection_schedules")
                    .whereGreaterThanOrEqualTo("collectionDateTime", tomorrow)
                    .whereLessThan("collectionDateTime", dayAfterTomorrow)
                    .whereEqualTo("isActive", true)
                    .get()
                    .get();

            // Query for schedules happening today
            QuerySnapshot todaySchedules = firestore.collection("collection_schedules")
                    .whereGreaterThanOrEqualTo("collectionDateTime", startOfToday)
                    .whereLessThan("collectionDateTime", endOfToday)
                    .whereEqualTo("isActive", true)
                    .get()
                    .get();

            // Send reminders for tomorrow's schedules
            for (var doc : tomorrowSchedules.getDocuments()) {
                CollectionSchedule schedule = doc.toObject(CollectionSchedule.class);
                notifyScheduleChange(schedule, "reminder");
            }

            // Send notifications for today's schedules
            for (var doc : todaySchedules.getDocuments()) {
                CollectionSchedule schedule = doc.toObject(CollectionSchedule.class);
                notifyScheduleChange(schedule, "today");
            }
        } catch (Exception e) {
            System.err.println("Error sending schedule reminders: " + e.getMessage());
        }
    }

    /**
     * Send a notification to a specific user
     * @param userId The ID of the user to send the notification to
     * @param title The notification title
     * @param body The notification body
     * @param data Additional data to send with the notification
     */
    public void sendNotificationToUser(String userId, String title, String body, Map<String, String> data) {
        try {
            // Get the user's FCM token
            DocumentSnapshot userDoc = firestore.collection("users").document(userId).get().get();
            User user = userDoc.toObject(User.class);
            
            if (user == null || user.getFcmToken() == null) {
                log.error("Cannot send notification: User {} has no FCM token", userId);
                return;
            }

            // Create message
            Message message = Message.builder()
                .setToken(user.getFcmToken())
                .setNotification(Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .build())
                .putAllData(data)
                .build();

            // Send message
            String response = firebaseMessaging.send(message);
            log.info("Successfully sent notification to user {}: {}", userId, response);
        } catch (Exception e) {
            log.error("Error sending notification to user {}: {}", userId, e.getMessage());
            throw new RuntimeException("Failed to send notification: " + e.getMessage());
        }
    }
} 