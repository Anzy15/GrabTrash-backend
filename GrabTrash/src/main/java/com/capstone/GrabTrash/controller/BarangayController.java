package com.capstone.GrabTrash.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capstone.GrabTrash.model.Barangay;
import com.capstone.GrabTrash.service.BarangayService;

@RestController
@RequestMapping("/api/barangays")
public class BarangayController {

    private final BarangayService barangayService;

    @Autowired
    public BarangayController(BarangayService barangayService) {
        this.barangayService = barangayService;
    }

    @PostMapping
    public ResponseEntity<?> addBarangay(@RequestBody Barangay barangay) {
        return barangayService.addBarangay(barangay);
    }

    @DeleteMapping("/{barangayId}")
    public ResponseEntity<?> removeBarangay(@PathVariable String barangayId) {
        return barangayService.removeBarangay(barangayId);
    }

    @PutMapping("/{barangayId}/reactivate")
    public ResponseEntity<?> reactivateBarangay(@PathVariable String barangayId) {
        return barangayService.reactivateBarangay(barangayId);
    }

    @GetMapping
    public ResponseEntity<?> getAllBarangays() {
        return barangayService.getAllBarangays();
    }

    @GetMapping("/{barangayId}")
    public ResponseEntity<?> getBarangayById(@PathVariable String barangayId) {
        return barangayService.getBarangayById(barangayId);
    }
} 