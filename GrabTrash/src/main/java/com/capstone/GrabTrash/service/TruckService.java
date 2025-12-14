package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.dto.TruckRequestDTO;
import com.capstone.GrabTrash.dto.TruckResponseDTO;
import com.capstone.GrabTrash.model.Truck;
import com.capstone.GrabTrash.model.User;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QuerySnapshot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Service for handling truck-related operations
 */
@Service
@Slf4j
public class TruckService {

    private static final String COLLECTION_NAME = "trucks";

    private final Firestore firestore;
    private final UserService userService;

    @Autowired
    public TruckService(Firestore firestore, UserService userService) {
        this.firestore = firestore;
        this.userService = userService;
    }

    /**
     * Create a new truck
     * @param truckRequest Truck information from admin
     * @return Truck response
     */
    public TruckResponseDTO createTruck(TruckRequestDTO truckRequest) {
        try {
            // Create a new truck record
            String truckId = UUID.randomUUID().toString();

            Truck truck = Truck.builder()
                    .truckId(truckId)
                    .size(truckRequest.getSize())
                    .wasteType(truckRequest.getWasteType())
                    .status(truckRequest.getStatus() != null ? truckRequest.getStatus() : "AVAILABLE") // Default to AVAILABLE if not specified
                    .make(truckRequest.getMake())
                    .model(truckRequest.getModel())
                    .plateNumber(truckRequest.getPlateNumber())
                    .truckPrice(truckRequest.getTruckPrice())
                    .capacity(truckRequest.getCapacity())
                    .driverId(truckRequest.getDriverId())
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .build();

            // Save the truck to Firestore
            firestore.collection(COLLECTION_NAME).document(truckId).set(truck);

            // Return the response
            return mapToResponseDTO(truck, "Truck created successfully");

        } catch (Exception e) {
            log.error("Error creating truck", e);
            throw new RuntimeException("Failed to create truck: " + e.getMessage());
        }
    }

    /**
     * Get all trucks
     * @return List of all trucks
     */
    public List<TruckResponseDTO> getAllTrucks() {
        try {
            CollectionReference trucksCollection = firestore.collection(COLLECTION_NAME);

            ApiFuture<QuerySnapshot> future = trucksCollection.get();
            List<Truck> trucks = future.get().toObjects(Truck.class);

            return mapToResponseDTOList(trucks);

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting all trucks", e);
            throw new RuntimeException("Failed to get trucks: " + e.getMessage());
        }
    }

    /**
     * Get truck by ID
     * @param truckId Truck ID
     * @return Truck information
     */
    public TruckResponseDTO getTruckById(String truckId) {
        try {
            Truck truck = firestore.collection(COLLECTION_NAME).document(truckId).get().get().toObject(Truck.class);

            if (truck == null) {
                throw new RuntimeException("Truck not found with ID: " + truckId);
            }

            return mapToResponseDTO(truck, "Truck retrieved successfully");

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting truck by ID", e);
            throw new RuntimeException("Failed to get truck: " + e.getMessage());
        }
    }

    /**
     * Update truck
     * @param truckId Truck ID
     * @param truckRequest Updated truck information
     * @return Updated truck response
     */
    public TruckResponseDTO updateTruck(String truckId, TruckRequestDTO truckRequest) {
        try {
            Truck existingTruck = firestore.collection(COLLECTION_NAME).document(truckId).get().get().toObject(Truck.class);

            if (existingTruck == null) {
                throw new RuntimeException("Truck not found with ID: " + truckId);
            }

            // Update the truck fields
            existingTruck.setSize(truckRequest.getSize());
            existingTruck.setWasteType(truckRequest.getWasteType());
            
            // Update status if provided
            if (truckRequest.getStatus() != null) {
                existingTruck.setStatus(truckRequest.getStatus());
            }
            
            // Update make, model, and plateNumber if provided
            if (truckRequest.getMake() != null) {
                existingTruck.setMake(truckRequest.getMake());
            }
            
            if (truckRequest.getModel() != null) {
                existingTruck.setModel(truckRequest.getModel());
            }
            
            if (truckRequest.getPlateNumber() != null) {
                existingTruck.setPlateNumber(truckRequest.getPlateNumber());
            }
            
            // Update truckPrice if provided
            if (truckRequest.getTruckPrice() != null) {
                existingTruck.setTruckPrice(truckRequest.getTruckPrice());
            }
            
            // Update capacity if provided
            if (truckRequest.getCapacity() != null) {
                existingTruck.setCapacity(truckRequest.getCapacity());
            }
            
            // Update driverId if provided
            if (truckRequest.getDriverId() != null) {
                existingTruck.setDriverId(truckRequest.getDriverId());
            }
            
            existingTruck.setUpdatedAt(new Date());

            // Save the updated truck to Firestore
            firestore.collection(COLLECTION_NAME).document(truckId).set(existingTruck);

            return mapToResponseDTO(existingTruck, "Truck updated successfully");

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error updating truck", e);
            throw new RuntimeException("Failed to update truck: " + e.getMessage());
        }
    }

    /**
     * Delete truck
     * @param truckId Truck ID
     * @return Success message
     */
    public TruckResponseDTO deleteTruck(String truckId) {
        try {
            Truck existingTruck = firestore.collection(COLLECTION_NAME).document(truckId).get().get().toObject(Truck.class);

            if (existingTruck == null) {
                throw new RuntimeException("Truck not found with ID: " + truckId);
            }

            // Delete the truck from Firestore
            firestore.collection(COLLECTION_NAME).document(truckId).delete();

            return TruckResponseDTO.builder()
                    .truckId(truckId)
                    .message("Truck deleted successfully")
                    .build();

        } catch (InterruptedException | ExecutionException e) {
            log.error("Error deleting truck", e);
            throw new RuntimeException("Failed to delete truck: " + e.getMessage());
        }
    }

    /**
     * Get trucks by waste type
     * @param wasteType Waste type
     * @return List of trucks for the waste type
     */
    public List<TruckResponseDTO> getTrucksByWasteType(String wasteType) {
        try {
            CollectionReference trucksCollection = firestore.collection(COLLECTION_NAME);
            Query query = trucksCollection.whereEqualTo("wasteType", wasteType);
            ApiFuture<QuerySnapshot> future = query.get();
            List<Truck> trucks = future.get().toObjects(Truck.class);
            return mapToResponseDTOList(trucks);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting trucks by waste type", e);
            throw new RuntimeException("Failed to get trucks for waste type: " + e.getMessage());
        }
    }

    /**
     * Get trucks by status
     * @param status Status to filter by (AVAILABLE, BUSY, etc.)
     * @return List of trucks matching the status
     */
    public List<TruckResponseDTO> getTrucksByStatus(String status) {
        try {
            CollectionReference trucksCollection = firestore.collection(COLLECTION_NAME);
            Query query = trucksCollection.whereEqualTo("status", status);
            ApiFuture<QuerySnapshot> future = query.get();
            List<Truck> trucks = future.get().toObjects(Truck.class);
            return mapToResponseDTOList(trucks);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting trucks by status", e);
            throw new RuntimeException("Failed to get trucks for status: " + e.getMessage());
        }
    }

    /**
     * Map a Truck entity to a TruckResponseDTO
     * @param truck Truck entity
     * @param message Response message
     * @return TruckResponseDTO
     */
    private TruckResponseDTO mapToResponseDTO(Truck truck, String message) {
        return TruckResponseDTO.builder()
                .truckId(truck.getTruckId())
                .size(truck.getSize())
                .wasteType(truck.getWasteType())
                .status(truck.getStatus())
                .make(truck.getMake())
                .model(truck.getModel())
                .plateNumber(truck.getPlateNumber())
                .truckPrice(truck.getTruckPrice())
                .capacity(truck.getCapacity())
                .driverId(truck.getDriverId())
                .createdAt(truck.getCreatedAt())
                .updatedAt(truck.getUpdatedAt())
                .message(message)
                .build();
    }

    /**
     * Map a list of Truck entities to a list of TruckResponseDTOs
     * @param trucks List of Truck entities
     * @return List of TruckResponseDTOs
     */
    private List<TruckResponseDTO> mapToResponseDTOList(List<Truck> trucks) {
        List<TruckResponseDTO> responseDTOs = new ArrayList<>();
        for (Truck truck : trucks) {
            responseDTOs.add(mapToResponseDTO(truck, "Truck retrieved successfully"));
        }
        return responseDTOs;
    }
    
    /**
     * Assign a driver to a truck
     * @param truckId Truck ID
     * @param driverId Driver ID to assign
     * @return Updated truck response
     */
    public TruckResponseDTO assignDriverToTruck(String truckId, String driverId) {
        try {
            // Validate truck exists
            Truck truck = firestore.collection(COLLECTION_NAME).document(truckId).get().get().toObject(Truck.class);
            if (truck == null) {
                throw new RuntimeException("Truck not found with ID: " + truckId);
            }
            
            // Check if truck already has a driver assigned
            if (truck.getDriverId() != null && !truck.getDriverId().isEmpty()) {
                throw new RuntimeException("Truck already has a driver assigned. Driver ID: " + truck.getDriverId());
            }
            
            // Validate driver exists and is actually a driver
            User driver = userService.getUserById(driverId);
            if (driver == null) {
                throw new RuntimeException("Driver not found with ID: " + driverId);
            }
            if (!"DRIVER".equalsIgnoreCase(driver.getRole())) {
                throw new RuntimeException("User is not a driver. User role: " + driver.getRole());
            }
            
            // Check if driver is already assigned to another truck
            CollectionReference trucksCollection = firestore.collection(COLLECTION_NAME);
            Query driverQuery = trucksCollection.whereEqualTo("driverId", driverId);
            ApiFuture<QuerySnapshot> future = driverQuery.get();
            List<Truck> existingAssignments = future.get().toObjects(Truck.class);
            
            if (!existingAssignments.isEmpty()) {
                throw new RuntimeException("Driver is already assigned to another truck. Truck ID: " + existingAssignments.get(0).getTruckId());
            }
            
            // Assign driver to truck
            truck.setDriverId(driverId);
            truck.setUpdatedAt(new Date());
            
            // Save updated truck
            firestore.collection(COLLECTION_NAME).document(truckId).set(truck);
            
            return mapToResponseDTO(truck, "Driver assigned to truck successfully");
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error assigning driver to truck", e);
            throw new RuntimeException("Failed to assign driver to truck: " + e.getMessage());
        }
    }
    
    /**
     * Remove driver assignment from a truck
     * @param truckId Truck ID
     * @return Updated truck response
     */
    public TruckResponseDTO removeDriverFromTruck(String truckId) {
        try {
            // Validate truck exists
            Truck truck = firestore.collection(COLLECTION_NAME).document(truckId).get().get().toObject(Truck.class);
            if (truck == null) {
                throw new RuntimeException("Truck not found with ID: " + truckId);
            }
            
            // Check if truck has a driver assigned
            if (truck.getDriverId() == null || truck.getDriverId().isEmpty()) {
                throw new RuntimeException("No driver is currently assigned to this truck");
            }
            
            // Remove driver assignment
            truck.setDriverId(null);
            truck.setUpdatedAt(new Date());
            
            // Save updated truck
            firestore.collection(COLLECTION_NAME).document(truckId).set(truck);
            
            return mapToResponseDTO(truck, "Driver removed from truck successfully");
            
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error removing driver from truck", e);
            throw new RuntimeException("Failed to remove driver from truck: " + e.getMessage());
        }
    }
    
    /**
     * Get trucks by driver ID
     * @param driverId Driver ID
     * @return List of trucks assigned to the driver
     */
    public List<TruckResponseDTO> getTrucksByDriverId(String driverId) {
        try {
            CollectionReference trucksCollection = firestore.collection(COLLECTION_NAME);
            Query query = trucksCollection.whereEqualTo("driverId", driverId);
            ApiFuture<QuerySnapshot> future = query.get();
            List<Truck> trucks = future.get().toObjects(Truck.class);
            return mapToResponseDTOList(trucks);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting trucks by driver ID", e);
            throw new RuntimeException("Failed to get trucks for driver: " + e.getMessage());
        }
    }
    
    /**
     * Get trucks with no assigned driver
     * @return List of trucks with no driver assigned
     */
    public List<TruckResponseDTO> getUnassignedTrucks() {
        try {
            CollectionReference trucksCollection = firestore.collection(COLLECTION_NAME);
            Query query = trucksCollection.whereEqualTo("driverId", null);
            ApiFuture<QuerySnapshot> future = query.get();
            List<Truck> trucks = future.get().toObjects(Truck.class);
            
            // Also check for trucks with empty string as driverId
            Query emptyQuery = trucksCollection.whereEqualTo("driverId", "");
            ApiFuture<QuerySnapshot> emptyFuture = emptyQuery.get();
            List<Truck> emptyTrucks = emptyFuture.get().toObjects(Truck.class);
            
            trucks.addAll(emptyTrucks);
            return mapToResponseDTOList(trucks);
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error getting unassigned trucks", e);
            throw new RuntimeException("Failed to get unassigned trucks: " + e.getMessage());
        }
    }
    
    /**
     * Find available trucks that can handle the specified weight
     * @param requiredCapacity Minimum capacity required in kilograms
     * @param wasteType Type of waste (for filtering compatible trucks)
     * @return List of available trucks with sufficient capacity
     */
    public List<Truck> findAvailableTrucksByCapacity(double requiredCapacity, String wasteType) {
        try {
            log.info("Looking for trucks with capacity >= {} kg and waste type: {}", requiredCapacity, wasteType);
            
            CollectionReference trucksCollection = firestore.collection(COLLECTION_NAME);
            
            // First, get trucks with "AVAILABLE" status
            Query availableQuery = trucksCollection.whereEqualTo("status", "AVAILABLE");
            ApiFuture<QuerySnapshot> availableFuture = availableQuery.get();
            List<Truck> availableTrucks = availableFuture.get().toObjects(Truck.class);
            
            log.info("Found {} trucks with AVAILABLE status", availableTrucks.size());
            
            // Debug: Log all available trucks
            for (Truck truck : availableTrucks) {
                log.debug("Truck {}: capacity={}, driverId={}, wasteType={}, status={}", 
                    truck.getTruckId(), truck.getCapacity(), truck.getDriverId(), truck.getWasteType(), truck.getStatus());
            }
            
            // Filter trucks that:
            // 1. Have sufficient capacity
            // 2. Are not assigned to any payment/order (no truckId references in payments)
            // 3. Match the waste type (if specified)
            // 4. Have an assigned driver
            List<Truck> suitableTrucks = availableTrucks.stream()
                .filter(truck -> {
                    // Check capacity
                    if (truck.getCapacity() == null || truck.getCapacity() < requiredCapacity) {
                        log.debug("Truck {} filtered out: insufficient capacity ({} < {})", 
                            truck.getTruckId(), truck.getCapacity(), requiredCapacity);
                        return false;
                    }
                    
                    // Check if truck has a driver assigned
                    if (truck.getDriverId() == null || truck.getDriverId().isEmpty()) {
                        log.debug("Truck {} filtered out: no driver assigned", truck.getTruckId());
                        return false;
                    }
                    
                    // Check waste type compatibility (relaxed matching)
                    if (wasteType != null && !wasteType.isEmpty() && 
                        truck.getWasteType() != null && !truck.getWasteType().isEmpty()) {
                        // If both have waste types, they should match (case-insensitive)
                        if (!truck.getWasteType().equalsIgnoreCase(wasteType)) {
                            log.debug("Truck {} filtered out: waste type mismatch ({} != {})", 
                                truck.getTruckId(), truck.getWasteType(), wasteType);
                            return false;
                        }
                    }
                    
                    // Check if truck is not currently assigned to any active payment
                    boolean isAssigned = isTruckAssignedToActivePayment(truck.getTruckId());
                    if (isAssigned) {
                        log.debug("Truck {} filtered out: already assigned to an active payment", truck.getTruckId());
                        return false;
                    }
                    
                    log.info("Truck {} passed all filters: capacity={}, driverId={}, wasteType={}", 
                        truck.getTruckId(), truck.getCapacity(), truck.getDriverId(), truck.getWasteType());
                    return true;
                })
                .sorted((t1, t2) -> {
                    // Sort by capacity (smallest sufficient capacity first for optimal assignment)
                    return Double.compare(t1.getCapacity(), t2.getCapacity());
                })
                .collect(java.util.stream.Collectors.toList());
                
            log.info("Found {} suitable trucks after filtering", suitableTrucks.size());
            return suitableTrucks;
                
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error finding available trucks by capacity", e);
            throw new RuntimeException("Failed to find available trucks: " + e.getMessage());
        }
    }
    
    /**
     * Check if a truck is currently assigned to any active payment
     * @param truckId Truck ID to check
     * @return true if truck is assigned to an active payment, false otherwise
     */
    private boolean isTruckAssignedToActivePayment(String truckId) {
        try {
            // Query payments collection to see if this truck is assigned to any active payment
            CollectionReference paymentsCollection = firestore.collection("payments");
            Query query = paymentsCollection.whereEqualTo("truckId", truckId);
            ApiFuture<QuerySnapshot> future = query.get();
            List<Object> payments = future.get().toObjects(Object.class);
            
            // If there are any payments with this truck ID, it's considered assigned
            return !payments.isEmpty();
            
        } catch (Exception e) {
            log.warn("Error checking truck assignment status for truck: {}", truckId, e);
            // In case of error, assume truck is assigned to be safe
            return true;
        }
    }
} 