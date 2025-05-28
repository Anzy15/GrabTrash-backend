package com.capstone.GrabTrash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.capstone.GrabTrash.model.CollectionSchedule;
import com.capstone.GrabTrash.dto.CollectionScheduleDTO;
import com.capstone.GrabTrash.model.Barangay;
import com.capstone.GrabTrash.model.User;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Query;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.time.*;
import java.util.ArrayList;

@Service
public class CollectionScheduleService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private UserService userService;

    @Autowired
    private BarangayService barangayService;
    
    @Autowired
    private NotificationService notificationService;

    private String generateShortId() {
        long timestamp = System.currentTimeMillis();
        String timestampStr = Long.toString(timestamp, 36);
        
        Random random = new Random();
        String randomChars = "";
        for (int i = 0; i < 3; i++) {
            char c = (char) (random.nextInt(26) + 'a');
            randomChars += c;
        }
        
        return "sch-" + timestampStr.substring(timestampStr.length() - 4) + randomChars;
    }

    private boolean isAdminUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String userEmail = authentication.getName();
        User currentUser = userService.getUserByEmailOrUsername(userEmail);
        
        return currentUser != null && "admin".equalsIgnoreCase(currentUser.getRole());
    }

    private Timestamp parseDateTime(String dateTimeStr) throws DateTimeParseException {
        Instant instant = Instant.parse(dateTimeStr);
        return Timestamp.of(java.util.Date.from(instant));
    }

    private CollectionSchedule convertDTOToModel(CollectionScheduleDTO dto) {
        CollectionSchedule model = new CollectionSchedule();
        model.setScheduleId(dto.getScheduleId());
        model.setBarangayId(dto.getBarangayId());
        model.setBarangayName(dto.getBarangayName());
        if (dto.getCollectionDateTime() != null) {
            model.setCollectionDateTime(parseDateTime(dto.getCollectionDateTime()));
        }
        model.setWasteType(dto.getWasteType());
        model.setRecurring(dto.isRecurring());
        model.setRecurringDay(dto.getRecurringDay());
        model.setRecurringTime(dto.getRecurringTime());
        model.setActive(dto.isActive());
        model.setNotes(dto.getNotes());
        return model;
    }

    private CollectionScheduleDTO convertModelToDTO(CollectionSchedule model) {
        CollectionScheduleDTO dto = new CollectionScheduleDTO();
        dto.setScheduleId(model.getScheduleId());
        dto.setBarangayId(model.getBarangayId());
        dto.setBarangayName(model.getBarangayName());
        if (model.getCollectionDateTime() != null) {
            dto.setCollectionDateTime(model.getCollectionDateTime().toDate().toInstant().toString());
        }
        dto.setWasteType(model.getWasteType());
        dto.setRecurring(model.isRecurring());
        dto.setRecurringDay(model.getRecurringDay());
        dto.setRecurringTime(model.getRecurringTime());
        dto.setActive(model.isActive());
        dto.setNotes(model.getNotes());
        return dto;
    }

    public ResponseEntity<?> addSchedule(CollectionScheduleDTO scheduleDTO) {
        try {
            if (!isAdminUser()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Validate barangay exists and is active
            ResponseEntity<?> barangayResponse = barangayService.getBarangayById(scheduleDTO.getBarangayId());
            if (barangayResponse.getStatusCode() != HttpStatus.OK) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid barangay ID");
                return ResponseEntity.badRequest().body(error);
            }
            Barangay barangay = (Barangay) barangayResponse.getBody();
            scheduleDTO.setBarangayName(barangay.getName());

            // Convert DTO to model
            CollectionSchedule schedule = convertDTOToModel(scheduleDTO);

            // Generate ID and set timestamps
            schedule.setScheduleId(generateShortId());
            schedule.setCreatedAt(Timestamp.now());
            schedule.setUpdatedAt(Timestamp.now());

            // Save to Firestore
            firestore.collection("collection_schedules")
                    .document(schedule.getScheduleId())
                    .set(schedule);
                    
            // Send notification to customers in this barangay about the new schedule
            String notificationTitle = "New Garbage Collection Schedule";
            String notificationBody = "A new " + schedule.getWasteType() + " collection has been scheduled";
            
            if (schedule.isRecurring()) {
                notificationBody += " every " + schedule.getRecurringDay() + " at " + schedule.getRecurringTime();
            } else if (schedule.getCollectionDateTime() != null) {
                // Format date for better readability
                Date date = schedule.getCollectionDateTime().toDate();
                notificationBody += " on " + date.toString();
            }
            
            if (schedule.getNotes() != null && !schedule.getNotes().isEmpty()) {
                notificationBody += ". Note: " + schedule.getNotes();
            }
            
            Map<String, String> data = new HashMap<>();
            data.put("scheduleId", schedule.getScheduleId());
            data.put("type", "NEW_COLLECTION_SCHEDULE");
            
            notificationService.sendNotificationToBarangay(
                schedule.getBarangayId(),
                notificationTitle,
                notificationBody,
                data
            );

            return ResponseEntity.ok(convertModelToDTO(schedule));
        } catch (DateTimeParseException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid date format. Please use ISO 8601 format (e.g., 2024-03-15T10:00:00Z)");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to add schedule: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> updateSchedule(String scheduleId, CollectionScheduleDTO scheduleDTO) {
        try {
            if (!isAdminUser()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Check if schedule exists
            CollectionSchedule existingSchedule = firestore.collection("collection_schedules")
                    .document(scheduleId)
                    .get()
                    .get()
                    .toObject(CollectionSchedule.class);

            if (existingSchedule == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Schedule not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            // Validate barangay exists and get its name
            ResponseEntity<?> barangayResponse = barangayService.getBarangayById(scheduleDTO.getBarangayId());
            if (barangayResponse.getStatusCode() != HttpStatus.OK) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid barangay ID");
                return ResponseEntity.badRequest().body(error);
            }
            Barangay barangay = (Barangay) barangayResponse.getBody();
            scheduleDTO.setBarangayName(barangay.getName());

            // Convert DTO to model
            CollectionSchedule schedule = convertDTOToModel(scheduleDTO);

            // Update timestamps and ID
            schedule.setScheduleId(scheduleId);
            schedule.setCreatedAt(existingSchedule.getCreatedAt());
            schedule.setUpdatedAt(Timestamp.now());

            // Save to Firestore
            firestore.collection("collection_schedules")
                    .document(scheduleId)
                    .set(schedule);
                    
            // Send notification about schedule update
            String notificationTitle = "Garbage Collection Schedule Updated";
            String notificationBody = "The " + schedule.getWasteType() + " collection schedule has been updated";
            
            if (schedule.isRecurring()) {
                notificationBody += " to every " + schedule.getRecurringDay() + " at " + schedule.getRecurringTime();
            } else if (schedule.getCollectionDateTime() != null) {
                // Format date for better readability
                Date date = schedule.getCollectionDateTime().toDate();
                notificationBody += " to " + date.toString();
            }
            
            if (schedule.getNotes() != null && !schedule.getNotes().isEmpty()) {
                notificationBody += ". Note: " + schedule.getNotes();
            }
            
            Map<String, String> data = new HashMap<>();
            data.put("scheduleId", schedule.getScheduleId());
            data.put("type", "UPDATED_COLLECTION_SCHEDULE");
            
            notificationService.sendNotificationToBarangay(
                schedule.getBarangayId(),
                notificationTitle,
                notificationBody,
                data
            );

            return ResponseEntity.ok(convertModelToDTO(schedule));
        } catch (DateTimeParseException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid date format. Please use ISO 8601 format (e.g., 2024-03-15T10:00:00Z)");
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update schedule: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> removeSchedule(String scheduleId) {
        try {
            if (!isAdminUser()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Check if schedule exists
            CollectionSchedule existingSchedule = firestore.collection("collection_schedules")
                    .document(scheduleId)
                    .get()
                    .get()
                    .toObject(CollectionSchedule.class);

            if (existingSchedule == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Schedule not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
            
            // Store barangay info before deletion for notification
            String barangayId = existingSchedule.getBarangayId();
            String wasteType = existingSchedule.getWasteType();

            // Delete from Firestore
            firestore.collection("collection_schedules")
                    .document(scheduleId)
                    .delete()
                    .get();
                    
            // Send notification about schedule cancellation
            String notificationTitle = "Garbage Collection Schedule Cancelled";
            String notificationBody = "The " + wasteType + " collection schedule has been cancelled.";
            
            Map<String, String> data = new HashMap<>();
            data.put("scheduleId", scheduleId);
            data.put("type", "CANCELLED_COLLECTION_SCHEDULE");
            
            notificationService.sendNotificationToBarangay(
                barangayId,
                notificationTitle,
                notificationBody,
                data
            );

            Map<String, String> response = new HashMap<>();
            response.put("message", "Schedule removed successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to remove schedule: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> getScheduleById(String scheduleId) {
        try {
            CollectionSchedule schedule = firestore.collection("collection_schedules")
                    .document(scheduleId)
                    .get()
                    .get()
                    .toObject(CollectionSchedule.class);

            if (schedule == null || !schedule.isActive()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Schedule not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

            return ResponseEntity.ok(convertModelToDTO(schedule));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get schedule: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> getSchedulesByBarangay(String barangayId) {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection("collection_schedules")
                    .whereEqualTo("barangayId", barangayId)
                    .whereEqualTo("isActive", true)
                    .get()
                    .get()
                    .getDocuments();

            List<CollectionScheduleDTO> schedules = documents.stream()
                    .map(doc -> convertModelToDTO(doc.toObject(CollectionSchedule.class)))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get schedules: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private List<Timestamp> getNextOccurrences(CollectionSchedule schedule, int count) {
        List<Timestamp> occurrences = new ArrayList<>();
        
        if (!schedule.isRecurring()) {
            occurrences.add(schedule.getCollectionDateTime());
            return occurrences;
        }

        // Convert schedule time to LocalDateTime
        LocalDateTime dateTime = LocalDateTime.ofInstant(
            schedule.getCollectionDateTime().toDate().toInstant(),
            ZoneId.systemDefault()
        );

        // Get day of week from recurringDay
        DayOfWeek targetDay = DayOfWeek.valueOf(schedule.getRecurringDay());
        
        // Parse recurring time
        LocalTime targetTime = LocalTime.parse(schedule.getRecurringTime());

        // Find next 'count' occurrences
        LocalDateTime current = dateTime;
        while (occurrences.size() < count) {
            if (current.toLocalDate().getDayOfWeek() != targetDay) {
                current = current.plusDays(1);
                continue;
            }

            // Set the correct time
            current = LocalDateTime.of(current.toLocalDate(), targetTime);
            
            // Only add future dates
            if (current.isAfter(LocalDateTime.now())) {
                occurrences.add(Timestamp.of(Date.from(current.atZone(ZoneId.systemDefault()).toInstant())));
            }
            
            // Move to next week
            current = current.plusWeeks(1);
        }

        return occurrences;
    }

    public ResponseEntity<?> getUpcomingSchedules(String barangayId) {
        try {
            // Get current timestamp
            Timestamp now = Timestamp.now();

            // Get all active schedules for the barangay
            List<QueryDocumentSnapshot> documents = firestore.collection("collection_schedules")
                    .whereEqualTo("barangayId", barangayId)
                    .whereEqualTo("isActive", true)
                    .get()
                    .get()
                    .getDocuments();

            // Process each schedule to get upcoming occurrences
            List<CollectionScheduleDTO> upcomingSchedules = new ArrayList<>();
            
            for (QueryDocumentSnapshot doc : documents) {
                CollectionSchedule schedule = doc.toObject(CollectionSchedule.class);
                
                // Get next 4 occurrences for recurring schedules, or just the single date for non-recurring
                List<Timestamp> nextOccurrences = getNextOccurrences(schedule, 4);
                
                // Create a DTO for each occurrence
                for (Timestamp occurrence : nextOccurrences) {
                    CollectionScheduleDTO dto = convertModelToDTO(schedule);
                    dto.setCollectionDateTime(occurrence.toDate().toInstant().toString());
                    upcomingSchedules.add(dto);
                }
            }

            // Sort by date and limit to 10
            upcomingSchedules.sort((a, b) -> {
                Instant timeA = Instant.parse(a.getCollectionDateTime());
                Instant timeB = Instant.parse(b.getCollectionDateTime());
                return timeA.compareTo(timeB);
            });

            if (upcomingSchedules.size() > 10) {
                upcomingSchedules = upcomingSchedules.subList(0, 10);
            }

            return ResponseEntity.ok(upcomingSchedules);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get upcoming schedules: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> getRecurringSchedules(String barangayId) {
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection("collection_schedules")
                    .whereEqualTo("barangayId", barangayId)
                    .whereEqualTo("isActive", true)
                    .whereEqualTo("isRecurring", true)
                    .get()
                    .get()
                    .getDocuments();

            List<CollectionScheduleDTO> schedules = documents.stream()
                    .map(doc -> convertModelToDTO(doc.toObject(CollectionSchedule.class)))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get recurring schedules: " + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // Method to send collection reminders for today's schedules
    public void sendTodayCollectionReminders() {
        try {
            // Get current date (without time)
            LocalDate today = LocalDate.now();
            
            // For one-time schedules, find those happening today
            List<CollectionSchedule> todaySchedules = firestore.collection("collection_schedules")
                .whereEqualTo("isRecurring", false)
                .whereEqualTo("isActive", true)
                .get()
                .get()
                .toObjects(CollectionSchedule.class)
                .stream()
                .filter(schedule -> {
                    if (schedule.getCollectionDateTime() == null) return false;
                    LocalDate scheduleDate = LocalDate.ofInstant(
                        schedule.getCollectionDateTime().toDate().toInstant(),
                        ZoneId.systemDefault()
                    );
                    return scheduleDate.equals(today);
                })
                .collect(Collectors.toList());
                
            // For recurring schedules, find those happening on today's day of week
            String dayOfWeek = today.getDayOfWeek().toString();
            List<CollectionSchedule> recurringSchedules = firestore.collection("collection_schedules")
                .whereEqualTo("isRecurring", true)
                .whereEqualTo("isActive", true)
                .whereEqualTo("recurringDay", dayOfWeek)
                .get()
                .get()
                .toObjects(CollectionSchedule.class);
                
            // Combine the lists
            List<CollectionSchedule> allTodaySchedules = new ArrayList<>();
            allTodaySchedules.addAll(todaySchedules);
            allTodaySchedules.addAll(recurringSchedules);
            
            // Send notifications for each schedule
            for (CollectionSchedule schedule : allTodaySchedules) {
                String notificationTitle = "Garbage Collection Today";
                String notificationBody = schedule.getWasteType() + " collection is scheduled for today";
                
                if (schedule.isRecurring() && schedule.getRecurringTime() != null) {
                    notificationBody += " at " + schedule.getRecurringTime();
                }
                
                if (schedule.getNotes() != null && !schedule.getNotes().isEmpty()) {
                    notificationBody += ". Note: " + schedule.getNotes();
                }
                
                Map<String, String> data = new HashMap<>();
                data.put("scheduleId", schedule.getScheduleId());
                data.put("type", "TODAY_COLLECTION_REMINDER");
                
                notificationService.sendNotificationToBarangay(
                    schedule.getBarangayId(),
                    notificationTitle,
                    notificationBody,
                    data
                );
            }
        } catch (Exception e) {
            // Log error but don't throw exception to prevent disrupting scheduled tasks
            System.err.println("Error sending collection reminders: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 