package com.capstone.GrabTrash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrivateEntityUpdateRequest {
    @JsonProperty("entityId")
    private String entityId;

    @JsonProperty("entityName")
    private String entityName;

    @JsonProperty("latitude")
    private Double latitude;

    @JsonProperty("longitude")
    private Double longitude;

    @JsonProperty("entityWasteType")
    private String entityWasteType;

    @JsonProperty("address")
    private String address;

    @JsonProperty("entityStatus")
    private String entityStatus;

    public PrivateEntityUpdateRequest() {}

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getEntityWasteType() {
        return entityWasteType;
    }

    public void setEntityWasteType(String entityWasteType) {
        this.entityWasteType = entityWasteType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEntityStatus() {
        return entityStatus;
    }

    public void setEntityStatus(String entityStatus) {
        this.entityStatus = entityStatus;
    }
} 