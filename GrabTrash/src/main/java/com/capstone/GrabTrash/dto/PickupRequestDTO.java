package com.capstone.GrabTrash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class PickupRequestDTO {
    @JsonProperty("location")
    private Map<String, Double> location;

    @JsonProperty("address")
    private String address;

    @JsonProperty("description")
    private String description;

    @JsonProperty("trashType")
    private String trashType;

    public PickupRequestDTO() {
    }

    public PickupRequestDTO(Map<String, Double> location, String address, String description, String trashType) {
        this.location = location;
        this.address = address;
        this.description = description;
        this.trashType = trashType;
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

    public String getTrashType() {
        return trashType;
    }

    public void setTrashType(String trashType) {
        this.trashType = trashType;
    }
} 