package com.capstone.GrabTrash.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.PropertyName;

public class Barangay {
    private String barangayId;
    private String name;
    private String description;
    private Timestamp createdAt;
    private boolean isActive;

    public Barangay() {
        this.isActive = true;
    }

    @PropertyName("barangayId")
    public String getBarangayId() {
        return barangayId;
    }

    @PropertyName("barangayId")
    public void setBarangayId(String barangayId) {
        this.barangayId = barangayId;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("createdAt")
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @PropertyName("createdAt")
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("isActive")
    public boolean isActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Barangay{" +
                "barangayId='" + barangayId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", createdAt=" + createdAt +
                ", isActive=" + isActive +
                '}';
    }
} 