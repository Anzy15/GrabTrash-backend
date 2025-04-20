package com.capstone.GrabTrash.model;

import com.google.cloud.firestore.annotation.PropertyName;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class PickupSchedule {
    private String id;
    private String date;
    private String time;
    private PickupLocation location;
    private String status;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_TIME;

    // Default constructor
    public PickupSchedule() {}

    // Constructor with all fields using LocalDate and LocalTime
    public PickupSchedule(String id, LocalDate date, LocalTime time, PickupLocation location, String status) {
        this.id = id;
        setDateFromLocalDate(date);
        setTimeFromLocalTime(time);
        this.location = location;
        this.status = status;
    }

    @PropertyName("id")
    public String getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("date")
    public String getDate() {
        return date;
    }

    @PropertyName("date")
    public void setDate(String date) {
        this.date = date;
    }

    // Helper methods for date conversion
    public LocalDate toLocalDate() {
        return date != null ? LocalDate.parse(date, DATE_FORMATTER) : null;
    }

    public void setDateFromLocalDate(LocalDate date) {
        this.date = date != null ? date.format(DATE_FORMATTER) : null;
    }

    @PropertyName("time")
    public String getTime() {
        return time;
    }

    @PropertyName("time")
    public void setTime(String time) {
        this.time = time;
    }

    // Helper methods for time conversion
    public LocalTime toLocalTime() {
        return time != null ? LocalTime.parse(time, TIME_FORMATTER) : null;
    }

    public void setTimeFromLocalTime(LocalTime time) {
        this.time = time != null ? time.format(TIME_FORMATTER) : null;
    }

    @PropertyName("location")
    public PickupLocation getLocation() {
        return location;
    }

    @PropertyName("location")
    public void setLocation(PickupLocation location) {
        this.location = location;
    }

    @PropertyName("status")
    public String getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }
} 