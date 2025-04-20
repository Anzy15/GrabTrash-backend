package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.model.PickupLocation;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class PickupLocationService {

    private static final String COLLECTION_NAME = "pickup_locations";

    @Autowired
    private Firestore firestore;

    /**
     * Get all pickup locations
     * @return List of pickup locations
     */
    public List<PickupLocation> getAllPickupLocations() throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        
        List<PickupLocation> locations = new ArrayList<>();
        for (DocumentSnapshot document : documents) {
            PickupLocation location = document.toObject(PickupLocation.class);
            if (location != null) {
                location.setId(document.getId());
                locations.add(location);
            }
        }
        
        return locations;
    }

    /**
     * Get a pickup location by ID
     * @param locationId The ID of the location to retrieve
     * @return The pickup location, or null if not found
     */
    public PickupLocation getLocationById(String locationId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(locationId);
        DocumentSnapshot document = docRef.get().get();
        
        if (document.exists()) {
            PickupLocation location = document.toObject(PickupLocation.class);
            if (location != null) {
                location.setId(document.getId());
                return location;
            }
        }
        
        return null;
    }

    /**
     * Create a new pickup location
     * @param location The pickup location data to save
     * @return The created pickup location with ID
     */
    public PickupLocation createPickupLocation(PickupLocation location) throws ExecutionException, InterruptedException {
        // Create a new document with auto-generated ID
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document();
        location.setId(docRef.getId());
        
        // Save the document
        ApiFuture<WriteResult> result = docRef.set(location);
        result.get(); // Wait for the write to complete
        
        return location;
    }

    /**
     * Update an existing pickup location
     * @param locationId The ID of the location to update
     * @param location The updated location data
     * @return The updated pickup location
     */
    public PickupLocation updatePickupLocation(String locationId, PickupLocation location) 
            throws ExecutionException, InterruptedException, IllegalArgumentException {
        
        // Check if the location exists
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(locationId);
        DocumentSnapshot document = docRef.get().get();
        
        if (!document.exists()) {
            throw new IllegalArgumentException("Pickup location with ID " + locationId + " not found");
        }
        
        // Set the ID
        location.setId(locationId);
        
        // Update the document
        ApiFuture<WriteResult> result = docRef.set(location);
        result.get(); // Wait for the write to complete
        
        return location;
    }

    /**
     * Delete a pickup location
     * @param locationId The ID of the location to delete
     * @return true if deletion was successful
     */
    public boolean deletePickupLocation(String locationId) 
            throws ExecutionException, InterruptedException, IllegalArgumentException {
        
        // Check if the location exists
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(locationId);
        DocumentSnapshot document = docRef.get().get();
        
        if (!document.exists()) {
            throw new IllegalArgumentException("Pickup location with ID " + locationId + " not found");
        }
        
        // Delete the document
        ApiFuture<WriteResult> result = docRef.delete();
        result.get(); // Wait for the delete to complete
        
        return true;
    }
} 