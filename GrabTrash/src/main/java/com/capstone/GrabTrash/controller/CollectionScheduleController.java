package com.capstone.GrabTrash.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capstone.GrabTrash.dto.CollectionScheduleDTO;
import com.capstone.GrabTrash.service.CollectionScheduleService;

@RestController
@RequestMapping("/api/collection-schedules")
public class CollectionScheduleController {

    private final CollectionScheduleService scheduleService;

    @Autowired
    public CollectionScheduleController(CollectionScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping
    public ResponseEntity<?> addSchedule(@RequestBody CollectionScheduleDTO scheduleDTO) {
        return scheduleService.addSchedule(scheduleDTO);
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<?> updateSchedule(
            @PathVariable String scheduleId,
            @RequestBody CollectionScheduleDTO scheduleDTO) {
        return scheduleService.updateSchedule(scheduleId, scheduleDTO);
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> removeSchedule(@PathVariable String scheduleId) {
        return scheduleService.removeSchedule(scheduleId);
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<?> getScheduleById(@PathVariable String scheduleId) {
        return scheduleService.getScheduleById(scheduleId);
    }

    @GetMapping("/barangay/{barangayId}")
    public ResponseEntity<?> getSchedulesByBarangay(@PathVariable String barangayId) {
        return scheduleService.getSchedulesByBarangay(barangayId);
    }

    @GetMapping("/barangay/{barangayId}/upcoming")
    public ResponseEntity<?> getUpcomingSchedules(@PathVariable String barangayId) {
        return scheduleService.getUpcomingSchedules(barangayId);
    }

    @GetMapping("/barangay/{barangayId}/recurring")
    public ResponseEntity<?> getRecurringSchedules(@PathVariable String barangayId) {
        return scheduleService.getRecurringSchedules(barangayId);
    }
} 