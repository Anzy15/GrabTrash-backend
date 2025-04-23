package com.capstone.GrabTrash.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.PropertyName;

public class CollectionSchedule {
    private String scheduleId;
    private String barangayId;
    private String barangayName; // For easier reference
    private Timestamp collectionDateTime;
    private String wasteType; // "Biodegradable" or "Non-Biodegradable"
    private boolean isRecurring;
    private String recurringDay; // Day of week for recurring schedules (e.g., "MONDAY")
    private String recurringTime; // Time for recurring schedules (e.g., "10:00")
    private boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String notes; // Optional notes about the collection

    public CollectionSchedule() {
        this.isActive = true;
    }

    @PropertyName("scheduleId")
    public String getScheduleId() {
        return scheduleId;
    }

    @PropertyName("scheduleId")
    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    @PropertyName("barangayId")
    public String getBarangayId() {
        return barangayId;
    }

    @PropertyName("barangayId")
    public void setBarangayId(String barangayId) {
        this.barangayId = barangayId;
    }

    @PropertyName("barangayName")
    public String getBarangayName() {
        return barangayName;
    }

    @PropertyName("barangayName")
    public void setBarangayName(String barangayName) {
        this.barangayName = barangayName;
    }

    @PropertyName("collectionDateTime")
    public Timestamp getCollectionDateTime() {
        return collectionDateTime;
    }

    @PropertyName("collectionDateTime")
    public void setCollectionDateTime(Timestamp collectionDateTime) {
        this.collectionDateTime = collectionDateTime;
    }

    @PropertyName("wasteType")
    public String getWasteType() {
        return wasteType;
    }

    @PropertyName("wasteType")
    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }

    @PropertyName("isRecurring")
    public boolean isRecurring() {
        return isRecurring;
    }

    @PropertyName("isRecurring")
    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    @PropertyName("recurringDay")
    public String getRecurringDay() {
        return recurringDay;
    }

    @PropertyName("recurringDay")
    public void setRecurringDay(String recurringDay) {
        this.recurringDay = recurringDay;
    }

    @PropertyName("recurringTime")
    public String getRecurringTime() {
        return recurringTime;
    }

    @PropertyName("recurringTime")
    public void setRecurringTime(String recurringTime) {
        this.recurringTime = recurringTime;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    @PropertyName("createdAt")
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @PropertyName("createdAt")
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("updatedAt")
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    @PropertyName("updatedAt")
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PropertyName("notes")
    public String getNotes() {
        return notes;
    }

    @PropertyName("notes")
    public void setNotes(String notes) {
        this.notes = notes;
    }
} 