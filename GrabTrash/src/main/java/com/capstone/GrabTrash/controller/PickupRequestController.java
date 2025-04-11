package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.dto.PickupRequestDTO;
import com.capstone.GrabTrash.service.PickupRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pickup-requests")
public class PickupRequestController {

    private final PickupRequestService pickupRequestService;

    @Autowired
    public PickupRequestController(PickupRequestService pickupRequestService) {
        this.pickupRequestService = pickupRequestService;
    }

    @PostMapping
    public ResponseEntity<?> createPickupRequest(@RequestBody PickupRequestDTO request) {
        return pickupRequestService.createPickupRequest(request);
    }

    @GetMapping
    public ResponseEntity<?> getUserPickupRequests() {
        return pickupRequestService.getUserPickupRequests();
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<?> getPickupRequest(@PathVariable String requestId) {
        return pickupRequestService.getPickupRequest(requestId);
    }

    @PutMapping("/{requestId}/status")
    public ResponseEntity<?> updatePickupRequestStatus(
            @PathVariable String requestId,
            @RequestBody Map<String, Object> request) {
        
        String status = (String) request.get("status");
        Double trashWeight = null;
        
        if (request.containsKey("trashWeight")) {
            Object weightObj = request.get("trashWeight");
            if (weightObj instanceof Number) {
                trashWeight = ((Number) weightObj).doubleValue();
            }
        }
        
        return pickupRequestService.updatePickupRequestStatus(requestId, status, trashWeight);
    }

    @GetMapping("/total-trash")
    public ResponseEntity<?> getTotalTrashPickedUp() {
        return pickupRequestService.getTotalTrashPickedUp();
    }
} 