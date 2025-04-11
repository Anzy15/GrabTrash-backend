package com.capstone.GrabTrash.model;

import com.google.cloud.Timestamp;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class PickupRequest {
    @JsonProperty("requestId")
    private String requestId;

    @JsonProperty("userId")
    private String userId;

    @JsonProperty("status")
    private String status; // PENDING, APPROVED, REJECTED, COMPLETED

    @JsonProperty("location")
    private Map<String, Double> location; // latitude and longitude

    @JsonProperty("address")
    private String address;

    @JsonProperty("description")
    private String description;

    @JsonProperty("createdAt")
    private Timestamp createdAt;

    @JsonProperty("updatedAt")
    private Timestamp updatedAt;

    @JsonProperty("completedAt")
    private Timestamp completedAt;

    @JsonProperty("trashWeight")
    private Double trashWeight; // in kilograms

    @JsonProperty("trashType")
    private String trashType; // RECYCLABLE, NON_RECYCLABLE, HAZARDOUS

    public PickupRequest() {
    }

    public PickupRequest(String requestId, String userId, String status, Map<String, Double> location, 
                         String address, String description, Timestamp createdAt, Timestamp updatedAt, 
                         Timestamp completedAt, Double trashWeight, String trashType) {
        this.requestId = requestId;
        this.userId = userId;
        this.status = status;
        this.location = location;
        this.address = address;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.completedAt = completedAt;
        this.trashWeight = trashWeight;
        this.trashType = trashType;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Map<String, Double> getLocation() {
        return location;
    }

    public void setLocation(Map<String, Double> location) {
        this.location = location;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Timestamp getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Timestamp completedAt) {
        this.completedAt = completedAt;
    }

    public Double getTrashWeight() {
        return trashWeight;
    }

    public void setTrashWeight(Double trashWeight) {
        this.trashWeight = trashWeight;
    }

    public String getTrashType() {
        return trashType;
    }

    public void setTrashType(String trashType) {
        this.trashType = trashType;
    }
} 