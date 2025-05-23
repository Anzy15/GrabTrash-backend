package com.capstone.GrabTrash.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.firestore.annotation.PropertyName;

import java.util.Date;

/**
 * Model class for storing truck information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Truck {
    private String truckId;
    private String size;
    private String wasteType;
    private String status; // AVAILABLE, BUSY, MAINTENANCE, etc.
    private String make;   // Truck manufacturer (e.g., Toyota, Isuzu, etc.)
    private String model;  // Truck model (e.g., Dyna, Elf, etc.)
    private String plateNumber; // License plate number
    private Date createdAt;
    private Date updatedAt;
    
    // Pre-persist hook to set dates
    public void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        updatedAt = new Date();
    }

    @PropertyName("truckId")
    public String getTruckId() {
        return truckId;
    }

    @PropertyName("truckId")
    public void setTruckId(String truckId) {
        this.truckId = truckId;
    }

    @PropertyName("size")
    public String getSize() {
        return size;
    }

    @PropertyName("size")
    public void setSize(String size) {
        this.size = size;
    }

    @PropertyName("wasteType")
    public String getWasteType() {
        return wasteType;
    }

    @PropertyName("wasteType")
    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }
    
    @PropertyName("status")
    public String getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }
    
    @PropertyName("make")
    public String getMake() {
        return make;
    }

    @PropertyName("make")
    public void setMake(String make) {
        this.make = make;
    }
    
    @PropertyName("model")
    public String getModel() {
        return model;
    }

    @PropertyName("model")
    public void setModel(String model) {
        this.model = model;
    }
    
    @PropertyName("plateNumber")
    public String getPlateNumber() {
        return plateNumber;
    }

    @PropertyName("plateNumber")
    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    @PropertyName("createdAt")
    public Date getCreatedAt() {
        return createdAt;
    }

    @PropertyName("createdAt")
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("updatedAt")
    public Date getUpdatedAt() {
        return updatedAt;
    }

    @PropertyName("updatedAt")
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
} 