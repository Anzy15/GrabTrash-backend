package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.dto.TruckRequestDTO;
import com.capstone.GrabTrash.dto.TruckResponseDTO;
import com.capstone.GrabTrash.dto.TruckAssignmentDTO;
import com.capstone.GrabTrash.dto.DriverTruckAssignmentDTO;
import com.capstone.GrabTrash.dto.PaymentResponseDTO;
import com.capstone.GrabTrash.service.TruckService;
import com.capstone.GrabTrash.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * REST Controller for handling truck-related endpoints
 */
@RestController
@RequestMapping("/api/trucks")
public class TruckController {

    private final TruckService truckService;
    private final PaymentService paymentService;

    @Autowired
    public TruckController(TruckService truckService, PaymentService paymentService) {
        this.truckService = truckService;
        this.paymentService = paymentService;
    }

    /**
     * Create a new truck
     * Requires JWT authentication with admin role
     * @param truckRequest Truck information from admin
     * @return Truck confirmation response
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TruckResponseDTO> createTruck(@RequestBody TruckRequestDTO truckRequest) {
        TruckResponseDTO response = truckService.createTruck(truckRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all trucks
     * Accessible to all authenticated users
     * @return List of all trucks
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TruckResponseDTO>> getAllTrucks() {
        List<TruckResponseDTO> trucks = truckService.getAllTrucks();
        return ResponseEntity.ok(trucks);
    }

    /**
     * Get truck by ID
     * Requires JWT authentication with admin role
     * @param truckId Truck ID
     * @return Truck information
     */
    @GetMapping("/{truckId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TruckResponseDTO> getTruckById(@PathVariable String truckId) {
        TruckResponseDTO truck = truckService.getTruckById(truckId);
        return ResponseEntity.ok(truck);
    }

    /**
     * Update truck
     * Requires JWT authentication with admin role
     * @param truckId Truck ID
     * @param truckRequest Updated truck information
     * @return Updated truck response
     */
    @PutMapping("/{truckId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TruckResponseDTO> updateTruck(@PathVariable String truckId, @RequestBody TruckRequestDTO truckRequest) {
        TruckResponseDTO response = truckService.updateTruck(truckId, truckRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Update truck price
     * Requires JWT authentication with admin role
     * @param truckId Truck ID
     * @param truckRequest Updated truck information with price
     * @return Updated truck response
     */
    @PutMapping("/{truckId}/price")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TruckResponseDTO> updateTruckPrice(@PathVariable String truckId, @RequestBody TruckRequestDTO truckRequest) {
        try {
            // Get the existing truck first
            TruckResponseDTO existingTruck = truckService.getTruckById(truckId);
            
            // Create a new request with only the price updated
            TruckRequestDTO priceUpdateRequest = TruckRequestDTO.builder()
                .size(existingTruck.getSize())
                .wasteType(existingTruck.getWasteType())
                .status(existingTruck.getStatus())
                .make(existingTruck.getMake())
                .model(existingTruck.getModel())
                .plateNumber(existingTruck.getPlateNumber())
                .capacity(existingTruck.getCapacity())
                .driverId(existingTruck.getDriverId())
                .truckPrice(truckRequest.getTruckPrice())
                .build();
            
            // Update with all fields preserved
            TruckResponseDTO response = truckService.updateTruck(truckId, priceUpdateRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Delete truck
     * Requires JWT authentication with admin role
     * @param truckId Truck ID
     * @return Success response
     */
    @DeleteMapping("/{truckId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TruckResponseDTO> deleteTruck(@PathVariable String truckId) {
        TruckResponseDTO response = truckService.deleteTruck(truckId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get trucks by waste type
     * Requires JWT authentication with admin role
     * @param wasteType Waste type
     * @return List of trucks for the waste type
     */
    @GetMapping("/waste-type/{wasteType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TruckResponseDTO>> getTrucksByWasteType(@PathVariable String wasteType) {
        List<TruckResponseDTO> trucks = truckService.getTrucksByWasteType(wasteType);
        return ResponseEntity.ok(trucks);
    }

    /**
     * Get trucks by status
     * Requires JWT authentication with admin role
     * @param status Truck status (AVAILABLE, BUSY, MAINTENANCE, etc.)
     * @return List of trucks for the given status
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TruckResponseDTO>> getTrucksByStatus(@PathVariable String status) {
        List<TruckResponseDTO> trucks = truckService.getTrucksByStatus(status);
        return ResponseEntity.ok(trucks);
    }

    /**
     * Assign a truck to a payment
     * Requires JWT authentication with admin role
     * @param assignment Truck assignment details
     * @return Updated payment response
     */
    @PostMapping("/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponseDTO> assignTruckToPayment(@RequestBody TruckAssignmentDTO assignment) {
        PaymentResponseDTO response = paymentService.assignTruckToPayment(assignment.getPaymentId(), assignment.getTruckId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Release a truck from a payment (mark it as available again)
     * Requires JWT authentication with admin role
     * @param paymentId ID of the payment to release the truck from
     * @return Updated payment response
     */
    @PostMapping("/release/{paymentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponseDTO> releaseTruckFromPayment(@PathVariable String paymentId) {
        PaymentResponseDTO response = paymentService.releaseTruckFromPayment(paymentId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Assign a driver to a truck
     * Requires JWT authentication with admin role
     * @param assignment Driver-truck assignment information
     * @return Updated truck response
     */
    @PostMapping("/assign-driver")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TruckResponseDTO> assignDriverToTruck(@RequestBody DriverTruckAssignmentDTO assignment) {
        TruckResponseDTO response = truckService.assignDriverToTruck(assignment.getTruckId(), assignment.getDriverId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Remove driver assignment from a truck
     * Requires JWT authentication with admin role
     * @param truckId ID of the truck to remove driver from
     * @return Updated truck response
     */
    @DeleteMapping("/remove-driver/{truckId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TruckResponseDTO> removeDriverFromTruck(@PathVariable String truckId) {
        TruckResponseDTO response = truckService.removeDriverFromTruck(truckId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get trucks assigned to a specific driver
     * Requires JWT authentication with admin role
     * @param driverId Driver ID
     * @return List of trucks assigned to the driver
     */
    @GetMapping("/driver/{driverId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TruckResponseDTO>> getTrucksByDriverId(@PathVariable String driverId) {
        List<TruckResponseDTO> trucks = truckService.getTrucksByDriverId(driverId);
        return ResponseEntity.ok(trucks);
    }
    
    /**
     * Get trucks with no assigned driver
     * Requires JWT authentication with admin role
     * @return List of unassigned trucks
     */
    @GetMapping("/unassigned")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TruckResponseDTO>> getUnassignedTrucks() {
        List<TruckResponseDTO> trucks = truckService.getUnassignedTrucks();
        return ResponseEntity.ok(trucks);
    }
    
    /**
     * Debug endpoint: Find available trucks for specific weight and waste type
     * Requires JWT authentication with admin role
     * @param weight Required capacity in kg
     * @param wasteType Waste type (optional)
     * @return List of suitable trucks with debug information
     */
    @GetMapping("/debug/available")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> debugAvailableTrucks(
            @RequestParam double weight,
            @RequestParam(required = false) String wasteType) {
        
        Map<String, Object> debugInfo = new HashMap<>();
        
        try {
            // Get all trucks for debugging
            List<TruckResponseDTO> allTrucks = truckService.getAllTrucks();
            debugInfo.put("totalTrucks", allTrucks.size());
            debugInfo.put("allTrucks", allTrucks);
            
            // Find suitable trucks using the same logic as payment processing
            List<com.capstone.GrabTrash.model.Truck> suitableTrucks = 
                truckService.findAvailableTrucksByCapacity(weight, wasteType);
            
            debugInfo.put("requiredWeight", weight);
            debugInfo.put("wasteType", wasteType);
            debugInfo.put("suitableTrucksCount", suitableTrucks.size());
            debugInfo.put("suitableTrucks", suitableTrucks);
            
            return ResponseEntity.ok(debugInfo);
            
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            debugInfo.put("requiredWeight", weight);
            debugInfo.put("wasteType", wasteType);
            return ResponseEntity.ok(debugInfo);
        }
    }
} 