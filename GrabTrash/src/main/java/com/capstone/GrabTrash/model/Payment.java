package com.capstone.GrabTrash.model;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.google.cloud.firestore.annotation.PropertyName;

/**
 * Model class for storing payment information
 * This class represents a payment record in the system
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private String id;
    private String orderId;
    private String customerName;
    private String customerEmail;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double amount;
    private Double tax;
    private Double totalAmount;
    private String paymentMethod;
    private String paymentReference;
    private String notes;
    private String status;
    private Date createdAt;
    private Date updatedAt;
    private String barangayId;
    private String phoneNumber;
    private String driverId;  // ID of the assigned driver
    private String wasteType;  // Type of waste (e.g., RECYCLABLE, NON_RECYCLABLE, HAZARDOUS)
    private String truckId;   // ID of the assigned truck
    private String jobOrderStatus;  // Status of the job order (NEW, IN_PROGRESS, COMPLETED, CANCELLED)
    private Integer numberOfSacks; // Number of sacks for waste collection
    private String truckSize;  // Size of the truck (Small, Medium, Large)
    
    // Pre-persist hook to set dates
    public void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }
        updatedAt = new Date();
    }

    @PropertyName("driverId")
    public String getDriverId() {
        return driverId;
    }

    @PropertyName("driverId")
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
    
    @PropertyName("wasteType")
    public String getWasteType() {
        return wasteType;
    }

    @PropertyName("wasteType")
    public void setWasteType(String wasteType) {
        this.wasteType = wasteType;
    }
    
    @PropertyName("truckId")
    public String getTruckId() {
        return truckId;
    }

    @PropertyName("truckId")
    public void setTruckId(String truckId) {
        this.truckId = truckId;
    }
    
    @PropertyName("jobOrderStatus")
    public String getJobOrderStatus() {
        return jobOrderStatus;
    }

    @PropertyName("jobOrderStatus")
    public void setJobOrderStatus(String jobOrderStatus) {
        this.jobOrderStatus = jobOrderStatus;
    }
    
    @PropertyName("numberOfSacks")
    public Integer getNumberOfSacks() {
        return numberOfSacks;
    }

    @PropertyName("numberOfSacks")
    public void setNumberOfSacks(Integer numberOfSacks) {
        this.numberOfSacks = numberOfSacks;
        
        // Automatically set truck size based on number of sacks
        if (numberOfSacks != null) {
            if (numberOfSacks <= 20) {
                this.truckSize = "Small";
            } else if (numberOfSacks <= 50) {
                this.truckSize = "Medium";
            } else {
                this.truckSize = "Large";
            }
        }
    }
    
    @PropertyName("truckSize")
    public String getTruckSize() {
        return truckSize;
    }

    @PropertyName("truckSize")
    public void setTruckSize(String truckSize) {
        this.truckSize = truckSize;
    }
}