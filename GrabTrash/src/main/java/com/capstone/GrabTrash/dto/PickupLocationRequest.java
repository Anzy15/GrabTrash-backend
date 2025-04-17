package com.capstone.GrabTrash.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PickupLocationRequest {
    @NotBlank(message = "Site name cannot be blank")
    @JsonProperty("siteName")
    private String siteName;
    
    @NotBlank(message = "Waste type cannot be blank")
    @JsonProperty("wasteType")
    private String wasteType;
    
    @NotBlank(message = "Address cannot be blank")
    @JsonProperty("address")
    private String address;
    
    @NotNull(message = "Latitude cannot be null")
    @JsonProperty("latitude")
    private Double latitude;
    
    @NotNull(message = "Longitude cannot be null")
    @JsonProperty("longitude")
    private Double longitude;

    // Default constructor
    public PickupLocationRequest() {}

    // Constructor with all fields
    public PickupLocationRequest(String siteName, String wasteType, String address, Double latitude, Double longitude) {
        this.siteName = siteName;
        this.wasteType = wasteType;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getWasteType() {
        return wasteType;
    }

    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
} 
