package com.capstone.GrabTrash.dto;

/**
 * Data Transfer Object for dashboard statistics
 * This class is used to send summarized data for the dashboard
 */
public class DashboardStatsDTO {
    private int totalPickupTrashOrdered;
    private double totalRevenue;
    private int totalCompletedOrders;
    private String message;
    
    // Default constructor
    public DashboardStatsDTO() {
    }
    
    // All-args constructor
    public DashboardStatsDTO(int totalPickupTrashOrdered, double totalRevenue, int totalCompletedOrders, String message) {
        this.totalPickupTrashOrdered = totalPickupTrashOrdered;
        this.totalRevenue = totalRevenue;
        this.totalCompletedOrders = totalCompletedOrders;
        this.message = message;
    }
    
    // Getters
    public int getTotalPickupTrashOrdered() {
        return totalPickupTrashOrdered;
    }
    
    public double getTotalRevenue() {
        return totalRevenue;
    }
    
    public int getTotalCompletedOrders() {
        return totalCompletedOrders;
    }
    
    public String getMessage() {
        return message;
    }
    
    // Setters
    public void setTotalPickupTrashOrdered(int totalPickupTrashOrdered) {
        this.totalPickupTrashOrdered = totalPickupTrashOrdered;
    }
    
    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }
    
    public void setTotalCompletedOrders(int totalCompletedOrders) {
        this.totalCompletedOrders = totalCompletedOrders;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    // Builder static class
    public static class Builder {
        private int totalPickupTrashOrdered;
        private double totalRevenue;
        private int totalCompletedOrders;
        private String message;
        
        public Builder totalPickupTrashOrdered(int totalPickupTrashOrdered) {
            this.totalPickupTrashOrdered = totalPickupTrashOrdered;
            return this;
        }
        
        public Builder totalRevenue(double totalRevenue) {
            this.totalRevenue = totalRevenue;
            return this;
        }
        
        public Builder totalCompletedOrders(int totalCompletedOrders) {
            this.totalCompletedOrders = totalCompletedOrders;
            return this;
        }
        
        public Builder message(String message) {
            this.message = message;
            return this;
        }
        
        public DashboardStatsDTO build() {
            return new DashboardStatsDTO(totalPickupTrashOrdered, totalRevenue, totalCompletedOrders, message);
        }
    }
    
    // Static builder factory method
    public static Builder builder() {
        return new Builder();
    }
} 