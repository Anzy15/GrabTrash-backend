package com.capstone.GrabTrash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionScheduleDTO {
    private String scheduleId;
    private String barangayId;
    private String barangayName;
    private String collectionDateTime; // ISO 8601 format
    private String wasteType;
    private boolean isRecurring;
    private String recurringDay;
    private String recurringTime;
    private boolean isActive;
    private String notes;

    public CollectionScheduleDTO() {
        this.isActive = true;
    }

    @JsonProperty("scheduleId")
    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    @JsonProperty("barangayId")
    public String getBarangayId() {
        return barangayId;
    }

    public void setBarangayId(String barangayId) {
        this.barangayId = barangayId;
    }

    @JsonProperty("barangayName")
    public String getBarangayName() {
        return barangayName;
    }

    public void setBarangayName(String barangayName) {
        this.barangayName = barangayName;
    }

    @JsonProperty("collectionDateTime")
    public String getCollectionDateTime() {
        return collectionDateTime;
    }

    public void setCollectionDateTime(String collectionDateTime) {
        this.collectionDateTime = collectionDateTime;
    }

    @JsonProperty("wasteType")
    public String getWasteType() {
        return wasteType;
    }

    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }

    @JsonProperty("isRecurring")
    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    @JsonProperty("recurringDay")
    public String getRecurringDay() {
        return recurringDay;
    }

    public void setRecurringDay(String recurringDay) {
        this.recurringDay = recurringDay;
    }

    @JsonProperty("recurringTime")
    public String getRecurringTime() {
        return recurringTime;
    }

    public void setRecurringTime(String recurringTime) {
        this.recurringTime = recurringTime;
    }

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @JsonProperty("notes")
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
} 