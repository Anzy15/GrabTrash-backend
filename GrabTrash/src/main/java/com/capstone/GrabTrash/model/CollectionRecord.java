package com.capstone.GrabTrash.model;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.PropertyName;

public class CollectionRecord {
    private String recordId;
    private String driverId;
    private String barangay;
    private String collectionType; // "PRIVATE" or "PUBLIC"
    private double weight; // in kilograms
    private Timestamp collectionDate;
    private String status; // "COMPLETED", "PENDING", "CANCELLED"
    private String notes;

    // Default constructor
    public CollectionRecord() {}

    @PropertyName("recordId")
    public String getRecordId() {
        return recordId;
    }

    @PropertyName("recordId")
    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    @PropertyName("driverId")
    public String getDriverId() {
        return driverId;
    }

    @PropertyName("driverId")
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    @PropertyName("barangay")
    public String getBarangay() {
        return barangay;
    }

    @PropertyName("barangay")
    public void setBarangay(String barangay) {
        this.barangay = barangay;
    }

    @PropertyName("collectionType")
    public String getCollectionType() {
        return collectionType;
    }

    @PropertyName("collectionType")
    public void setCollectionType(String collectionType) {
        this.collectionType = collectionType;
    }

    @PropertyName("weight")
    public double getWeight() {
        return weight;
    }

    @PropertyName("weight")
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @PropertyName("collectionDate")
    public Timestamp getCollectionDate() {
        return collectionDate;
    }

    @PropertyName("collectionDate")
    public void setCollectionDate(Timestamp collectionDate) {
        this.collectionDate = collectionDate;
    }

    @PropertyName("status")
    public String getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
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