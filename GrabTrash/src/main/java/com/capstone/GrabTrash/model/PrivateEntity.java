package com.capstone.GrabTrash.model;

import com.google.cloud.firestore.annotation.PropertyName;

public class PrivateEntity {
    private String entityId;
    private String userId;
    private String entityName;
    private Double latitude;
    private Double longitude;
    private String entityWasteType;
    private String address;
    private String entityStatus;

    public PrivateEntity() {}

    public PrivateEntity(String entityId, String userId, String entityName, Double latitude, Double longitude, 
                        String entityWasteType, String address, String entityStatus) {
        this.entityId = entityId;
        this.userId = userId;
        this.entityName = entityName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.entityWasteType = entityWasteType;
        this.address = address;
        this.entityStatus = entityStatus;
    }

    @PropertyName("entityId")
    public String getEntityId() {
        return entityId;
    }

    @PropertyName("entityId")
    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("entityName")
    public String getEntityName() {
        return entityName;
    }

    @PropertyName("entityName")
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    @PropertyName("latitude")
    public Double getLatitude() {
        return latitude;
    }

    @PropertyName("latitude")
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    @PropertyName("longitude")
    public Double getLongitude() {
        return longitude;
    }

    @PropertyName("longitude")
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    @PropertyName("entityWasteType")
    public String getEntityWasteType() {
        return entityWasteType;
    }

    @PropertyName("entityWasteType")
    public void setEntityWasteType(String entityWasteType) {
        this.entityWasteType = entityWasteType;
    }

    @PropertyName("address")
    public String getAddress() {
        return address;
    }

    @PropertyName("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @PropertyName("entityStatus")
    public String getEntityStatus() {
        return entityStatus;
    }

    @PropertyName("entityStatus")
    public void setEntityStatus(String entityStatus) {
        this.entityStatus = entityStatus;
    }
} 