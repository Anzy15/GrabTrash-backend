package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.dto.PickupScheduleDTO;
import com.capstone.GrabTrash.model.PickupSchedule;
import com.capstone.GrabTrash.service.PickupScheduleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/pickup-schedules")
public class PickupScheduleController {
    private static final Logger logger = LoggerFactory.getLogger(PickupScheduleController.class);

    @Autowired
    private PickupScheduleService pickupScheduleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSchedule(@RequestBody PickupScheduleDTO scheduleDTO) {
        try {
            logger.info("Attempting to create schedule with data: {}", scheduleDTO);
            
            if (scheduleDTO.getDate() == null) {
                logger.error("Date is null in request");
                return ResponseEntity.badRequest().body("Date is required");
            }
            if (scheduleDTO.getTime() == null) {
                logger.error("Time is null in request");
                return ResponseEntity.badRequest().body("Time is required");
            }
            if (scheduleDTO.getLocationId() == null || scheduleDTO.getLocationId().trim().isEmpty()) {
                logger.error("LocationId is null or empty in request");
                return ResponseEntity.badRequest().body("LocationId is required");
            }

            PickupSchedule schedule = pickupScheduleService.createSchedule(scheduleDTO);
            logger.info("Successfully created schedule with ID: {}", schedule.getId());
            return ResponseEntity.ok(schedule);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request data: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating schedule: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error creating schedule: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSchedule(
            @PathVariable String id,
            @RequestBody PickupScheduleDTO scheduleDTO) {
        try {
            logger.info("Attempting to update schedule with ID: {} and data: {}", id, scheduleDTO);
            PickupSchedule schedule = pickupScheduleService.updateSchedule(id, scheduleDTO);
            logger.info("Successfully updated schedule with ID: {}", id);
            return ResponseEntity.ok(schedule);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request data for update: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error updating schedule: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error updating schedule: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSchedule(@PathVariable String id) {
        try {
            logger.info("Attempting to delete schedule with ID: {}", id);
            pickupScheduleService.deleteSchedule(id);
            logger.info("Successfully deleted schedule with ID: {}", id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request for deletion: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Error deleting schedule: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error deleting schedule: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<?> getScheduleById(@PathVariable String id) {
        try {
            logger.info("Fetching schedule with ID: {}", id);
            PickupSchedule schedule = pickupScheduleService.getScheduleById(id);
            if (schedule != null) {
                logger.info("Found schedule with ID: {}", id);
                return ResponseEntity.ok(schedule);
            }
            logger.warn("Schedule not found with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error fetching schedule: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching schedule: " + e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<?> getAllSchedules() {
        try {
            logger.info("Fetching all schedules");
            List<PickupSchedule> schedules = pickupScheduleService.getAllSchedules();
            logger.info("Found {} schedules", schedules.size());
            return ResponseEntity.ok(schedules);
        } catch (Exception e) {
            logger.error("Error fetching schedules: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error fetching schedules: " + e.getMessage());
        }
    }
} 