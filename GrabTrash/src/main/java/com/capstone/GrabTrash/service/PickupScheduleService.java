package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.dto.PickupScheduleDTO;
import com.capstone.GrabTrash.model.PickupLocation;
import com.capstone.GrabTrash.model.PickupSchedule;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class PickupScheduleService {
    private static final Logger logger = LoggerFactory.getLogger(PickupScheduleService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    @Autowired
    private Firestore firestore;

    @Autowired
    private PickupLocationService pickupLocationService;

    private static final String COLLECTION_NAME = "pickupSchedules";

    public PickupSchedule createSchedule(PickupScheduleDTO scheduleDTO) throws ExecutionException, InterruptedException {
        logger.debug("Creating new schedule with DTO: {}", scheduleDTO);

        // Validate input
        if (scheduleDTO.getDate() == null) {
            logger.error("Date is null");
            throw new IllegalArgumentException("Date is required");
        }
        if (scheduleDTO.getTime() == null) {
            logger.error("Time is null");
            throw new IllegalArgumentException("Time is required");
        }
        if (scheduleDTO.getLocationId() == null || scheduleDTO.getLocationId().trim().isEmpty()) {
            logger.error("LocationId is null or empty");
            throw new IllegalArgumentException("LocationId is required");
        }

        // Validate date is not in the past
        if (scheduleDTO.getDate().isBefore(LocalDate.now())) {
            logger.error("Schedule date is in the past: {}", scheduleDTO.getDate());
            throw new IllegalArgumentException("Schedule date cannot be in the past");
        }

        // Get and validate location
        logger.debug("Fetching location with ID: {}", scheduleDTO.getLocationId());
        PickupLocation location = pickupLocationService.getLocationById(scheduleDTO.getLocationId());
        if (location == null) {
            logger.error("Location not found with ID: {}", scheduleDTO.getLocationId());
            throw new IllegalArgumentException("Location not found with ID: " + scheduleDTO.getLocationId());
        }
        logger.debug("Found location: {}", location);

        // Create new schedule
        String scheduleId = UUID.randomUUID().toString();
        PickupSchedule schedule = new PickupSchedule();
        schedule.setId(scheduleId);
        schedule.setDateFromLocalDate(scheduleDTO.getDate());
        schedule.setTimeFromLocalTime(scheduleDTO.getTime());
        schedule.setLocation(location);
        schedule.setStatus(scheduleDTO.getStatus() != null ? scheduleDTO.getStatus() : "PENDING");

        try {
            // Save to Firestore
            logger.debug("Saving schedule to Firestore with ID: {}", scheduleId);
            firestore.collection(COLLECTION_NAME)
                    .document(scheduleId)
                    .set(schedule)
                    .get();
            logger.info("Successfully created schedule with ID: {}", scheduleId);

            return schedule;
        } catch (Exception e) {
            logger.error("Error saving schedule to Firestore: {}", e.getMessage(), e);
            throw new RuntimeException("Error saving schedule: " + e.getMessage());
        }
    }

    public PickupSchedule updateSchedule(String id, PickupScheduleDTO scheduleDTO) throws ExecutionException, InterruptedException {
        logger.debug("Updating schedule with ID: {} and DTO: {}", id, scheduleDTO);

        // Get existing schedule
        PickupSchedule existingSchedule = getScheduleById(id);
        if (existingSchedule == null) {
            logger.error("Schedule not found with ID: {}", id);
            throw new IllegalArgumentException("Schedule not found with ID: " + id);
        }

        // Validate and update fields
        if (scheduleDTO.getDate() != null) {
            if (scheduleDTO.getDate().isBefore(LocalDate.now())) {
                logger.error("Updated date is in the past: {}", scheduleDTO.getDate());
                throw new IllegalArgumentException("Schedule date cannot be in the past");
            }
            existingSchedule.setDateFromLocalDate(scheduleDTO.getDate());
        }

        if (scheduleDTO.getTime() != null) {
            existingSchedule.setTimeFromLocalTime(scheduleDTO.getTime());
        }

        if (scheduleDTO.getLocationId() != null && !scheduleDTO.getLocationId().trim().isEmpty()) {
            logger.debug("Updating location. Fetching new location with ID: {}", scheduleDTO.getLocationId());
            PickupLocation location = pickupLocationService.getLocationById(scheduleDTO.getLocationId());
            if (location == null) {
                logger.error("New location not found with ID: {}", scheduleDTO.getLocationId());
                throw new IllegalArgumentException("Location not found with ID: " + scheduleDTO.getLocationId());
            }
            existingSchedule.setLocation(location);
        }

        if (scheduleDTO.getStatus() != null) {
            existingSchedule.setStatus(scheduleDTO.getStatus());
        }

        try {
            // Save to Firestore
            logger.debug("Saving updated schedule to Firestore");
            firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .set(existingSchedule)
                    .get();
            logger.info("Successfully updated schedule with ID: {}", id);

            return existingSchedule;
        } catch (Exception e) {
            logger.error("Error updating schedule in Firestore: {}", e.getMessage(), e);
            throw new RuntimeException("Error updating schedule: " + e.getMessage());
        }
    }

    public void deleteSchedule(String id) throws ExecutionException, InterruptedException {
        logger.debug("Deleting schedule with ID: {}", id);

        // Verify schedule exists
        PickupSchedule schedule = getScheduleById(id);
        if (schedule == null) {
            logger.error("Schedule not found with ID: {}", id);
            throw new IllegalArgumentException("Schedule not found with ID: " + id);
        }

        try {
            firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .delete()
                    .get();
            logger.info("Successfully deleted schedule with ID: {}", id);
        } catch (Exception e) {
            logger.error("Error deleting schedule from Firestore: {}", e.getMessage(), e);
            throw new RuntimeException("Error deleting schedule: " + e.getMessage());
        }
    }

    public PickupSchedule getScheduleById(String id) throws ExecutionException, InterruptedException {
        logger.debug("Fetching schedule with ID: {}", id);
        try {
            PickupSchedule schedule = firestore.collection(COLLECTION_NAME)
                    .document(id)
                    .get()
                    .get()
                    .toObject(PickupSchedule.class);
            
            if (schedule == null) {
                logger.warn("No schedule found with ID: {}", id);
            } else {
                logger.debug("Found schedule: {}", schedule);
            }
            
            return schedule;
        } catch (Exception e) {
            logger.error("Error fetching schedule from Firestore: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching schedule: " + e.getMessage());
        }
    }

    public List<PickupSchedule> getAllSchedules() throws ExecutionException, InterruptedException {
        logger.debug("Fetching all schedules");
        List<PickupSchedule> schedules = new ArrayList<>();
        
        try {
            List<QueryDocumentSnapshot> documents = firestore.collection(COLLECTION_NAME)
                    .get()
                    .get()
                    .getDocuments();

            for (QueryDocumentSnapshot document : documents) {
                PickupSchedule schedule = document.toObject(PickupSchedule.class);
                if (schedule != null) {
                    schedules.add(schedule);
                }
            }
            
            logger.info("Found {} schedules", schedules.size());
            return schedules;
        } catch (Exception e) {
            logger.error("Error fetching schedules from Firestore: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching schedules: " + e.getMessage());
        }
    }
} 