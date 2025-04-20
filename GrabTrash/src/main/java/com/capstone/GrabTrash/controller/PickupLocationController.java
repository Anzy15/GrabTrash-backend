package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.model.PickupLocation;
import com.capstone.GrabTrash.service.PickupLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/pickup-locations")
public class PickupLocationController {

    @Autowired
    private PickupLocationService pickupLocationService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<PickupLocation>> getAllPickupLocations() {
        try {
            List<PickupLocation> locations = pickupLocationService.getAllPickupLocations();
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<PickupLocation> getPickupLocationById(@PathVariable String id) {
        try {
            PickupLocation location = pickupLocationService.getLocationById(id);
            if (location != null) {
                return ResponseEntity.ok(location);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PickupLocation> createPickupLocation(@RequestBody PickupLocation location) {
        try {
            PickupLocation createdLocation = pickupLocationService.createPickupLocation(location);
            return ResponseEntity.ok(createdLocation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PickupLocation> updatePickupLocation(
            @PathVariable String id,
            @RequestBody PickupLocation location) {
        try {
            PickupLocation updatedLocation = pickupLocationService.updatePickupLocation(id, location);
            return ResponseEntity.ok(updatedLocation);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePickupLocation(@PathVariable String id) {
        try {
            pickupLocationService.deletePickupLocation(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}