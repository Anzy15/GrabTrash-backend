package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.dto.TruckRequestDTO;
import com.capstone.GrabTrash.dto.TruckResponseDTO;
import com.capstone.GrabTrash.model.Truck;
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

    @Autowired
    public TruckService(Firestore firestore) {
        this.firestore = firestore;
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
} 