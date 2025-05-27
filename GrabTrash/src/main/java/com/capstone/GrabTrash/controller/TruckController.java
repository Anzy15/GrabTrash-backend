package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.dto.TruckRequestDTO;
import com.capstone.GrabTrash.dto.TruckResponseDTO;
import com.capstone.GrabTrash.dto.TruckAssignmentDTO;
import com.capstone.GrabTrash.dto.PaymentResponseDTO;
import com.capstone.GrabTrash.service.TruckService;
import com.capstone.GrabTrash.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for handling truck-related endpoints
 * All endpoints require JWT authentication with admin role
 */
@RestController
@RequestMapping("/api/trucks")
@PreAuthorize("hasRole('admin')")
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
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<TruckResponseDTO> createTruck(@RequestBody TruckRequestDTO truckRequest) {
        TruckResponseDTO response = truckService.createTruck(truckRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all trucks
     * Requires JWT authentication with admin role
     * @return List of all trucks
     */
    @GetMapping
    @PreAuthorize("hasRole('admin')")
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
    @PreAuthorize("hasRole('admin')")
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
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<TruckResponseDTO> updateTruck(@PathVariable String truckId, @RequestBody TruckRequestDTO truckRequest) {
        TruckResponseDTO response = truckService.updateTruck(truckId, truckRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * Delete truck
     * Requires JWT authentication with admin role
     * @param truckId Truck ID
     * @return Success response
     */
    @DeleteMapping("/{truckId}")
    @PreAuthorize("hasRole('admin')")
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
    @PreAuthorize("hasRole('admin')")
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
    @PreAuthorize("hasRole('admin')")
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
    @PreAuthorize("hasRole('admin')")
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
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<PaymentResponseDTO> releaseTruckFromPayment(@PathVariable String paymentId) {
        PaymentResponseDTO response = paymentService.releaseTruckFromPayment(paymentId);
        return ResponseEntity.ok(response);
    }
} 