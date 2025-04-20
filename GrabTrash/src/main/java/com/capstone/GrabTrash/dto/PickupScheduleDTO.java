package com.capstone.GrabTrash.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class PickupScheduleDTO {
    private String id;
    private LocalDate date;
    private LocalTime time;
    private String locationId;
    private String status;

    // Default constructor
    public PickupScheduleDTO() {}

    // Constructor with all fields
    public PickupScheduleDTO(String id, LocalDate date, LocalTime time, String locationId, String status) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.locationId = locationId;
        this.status = status;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
} 