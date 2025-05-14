package com.capstone.GrabTrash.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.capstone.GrabTrash.dto.PickupLocationRequest;
import com.capstone.GrabTrash.dto.PickupLocationResponse;
import com.capstone.GrabTrash.model.PickupLocation;
import com.capstone.GrabTrash.service.PickupLocationService;

import jakarta.validation.Valid;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/pickup-locations")
public class PickupLocationController {

    @Autowired
    private PickupLocationService pickupLocationService;

    /**
     * Get all pickup locations
     * This endpoint is publicly accessible (no JWT required)
     * @return List of all pickup locations
     */
    @GetMapping
    public ResponseEntity<PickupLocationResponse> getAllPickupLocations() {
        try {
            List<PickupLocation> locations = pickupLocationService.getAllPickupLocations();
            PickupLocationResponse response = new PickupLocationResponse(
                    locations,
                    true,
                    "Pickup locations retrieved successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PickupLocationResponse response = new PickupLocationResponse(
                    false,
                    "Error retrieving pickup locations: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get the total count of pickup locations
     * This endpoint is publicly accessible (no JWT required)
     * @return Total number of pickup locations
     */
    @GetMapping("/count")
    public ResponseEntity<PickupLocationResponse> getTotalPickupLocations() {
        try {
            long count = pickupLocationService.getTotalPickupLocations();
            PickupLocationResponse response = new PickupLocationResponse(
                    count,
                    true,
                    "Total pickup locations count retrieved successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PickupLocationResponse response = new PickupLocationResponse(
                    false,
                    "Error retrieving pickup locations count: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Get a pickup location by ID
     * This endpoint is publicly accessible (no JWT required)
     * @param id The ID of the pickup location to retrieve
     * @return The pickup location with the specified ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PickupLocationResponse> getPickupLocationById(@PathVariable String id) {
        try {
            PickupLocation location = pickupLocationService.getPickupLocationById(id);
            
            if (location == null) {
                PickupLocationResponse response = new PickupLocationResponse(
                        false,
                        "Pickup location not found with ID: " + id
                );
                return ResponseEntity.notFound().build();
            }
            
            PickupLocationResponse response = new PickupLocationResponse(
                    location,
                    true,
                    "Pickup location retrieved successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            PickupLocationResponse response = new PickupLocationResponse(
                    false,
                    "Error retrieving pickup location: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Create a new pickup location
     * This endpoint requires JWT authentication
     * @param request The pickup location data to create
     * @return The created pickup location
     */
    @PostMapping
    public ResponseEntity<PickupLocationResponse> createPickupLocation(
            @Valid @RequestBody PickupLocationRequest request,
            BindingResult bindingResult) {
        try {
            // Check for validation errors
            if (bindingResult.hasErrors()) {
                StringBuilder errorMessage = new StringBuilder("Validation failed: ");
                for (FieldError error : bindingResult.getFieldErrors()) {
                    errorMessage.append("\n- ")
                              .append(error.getField())
                              .append(": ")
                              .append(error.getDefaultMessage())
                              .append(" (Rejected value: ")
                              .append(error.getRejectedValue())
                              .append(")");
                }
                
                System.out.println("Request body: " + request); // Debug log
                System.out.println("Validation errors: " + errorMessage); // Debug log
                
                PickupLocationResponse response = new PickupLocationResponse(
                    false,
                    errorMessage.toString()
                );
                return ResponseEntity.badRequest().body(response);
            }

            // Debug log
            System.out.println("Creating pickup location with data:");
            System.out.println("Address: " + request.getAddress());
            System.out.println("Site Name: " + request.getSiteName());
            System.out.println("Waste Type: " + request.getWasteType());
            System.out.println("Latitude: " + request.getLatitude());
            System.out.println("Longitude: " + request.getLongitude());

            PickupLocation newLocation = new PickupLocation();
            newLocation.setSiteName(request.getSiteName());
            newLocation.setWasteType(request.getWasteType());
            newLocation.setAddress(request.getAddress());
            newLocation.setLatitude(request.getLatitude());
            newLocation.setLongitude(request.getLongitude());
            
            PickupLocation createdLocation = pickupLocationService.createPickupLocation(newLocation);
            
            PickupLocationResponse response = new PickupLocationResponse(
                createdLocation,
                true,
                "Pickup location created successfully"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // Debug log
            PickupLocationResponse response = new PickupLocationResponse(
                false,
                "Error creating pickup location: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Update an existing pickup location
     * This endpoint requires JWT authentication
     * @param id The ID of the pickup location to update
     * @param request The updated pickup location data
     * @return The updated pickup location
     */
    @PutMapping("/{id}")
    public ResponseEntity<PickupLocationResponse> updatePickupLocation(
            @PathVariable String id,
            @Valid @RequestBody PickupLocationRequest request) {
        try {
            PickupLocation updatedLocation = new PickupLocation();
            updatedLocation.setSiteName(request.getSiteName());
            updatedLocation.setWasteType(request.getWasteType());
            updatedLocation.setAddress(request.getAddress());
            updatedLocation.setLatitude(request.getLatitude());
            updatedLocation.setLongitude(request.getLongitude());
            
            PickupLocation result = pickupLocationService.updatePickupLocation(id, updatedLocation);
            
            PickupLocationResponse response = new PickupLocationResponse(
                    result,
                    true,
                    "Pickup location updated successfully"
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            PickupLocationResponse response = new PickupLocationResponse(
                    false,
                    e.getMessage()
            );
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            PickupLocationResponse response = new PickupLocationResponse(
                    false,
                    "Error updating pickup location: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Delete a pickup location
     * This endpoint requires JWT authentication
     * @param id The ID of the pickup location to delete
     * @return Success or error message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<PickupLocationResponse> deletePickupLocation(@PathVariable String id) {
        try {
            boolean deleted = pickupLocationService.deletePickupLocation(id);
            
            if (deleted) {
                PickupLocationResponse response = new PickupLocationResponse(
                        true,
                        "Pickup location deleted successfully"
                );
                return ResponseEntity.ok(response);
            } else {
                PickupLocationResponse response = new PickupLocationResponse(
                        false,
                        "Failed to delete pickup location"
                );
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            PickupLocationResponse response = new PickupLocationResponse(
                    false,
                    e.getMessage()
            );
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            PickupLocationResponse response = new PickupLocationResponse(
                    false,
                    "Error deleting pickup location: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
}