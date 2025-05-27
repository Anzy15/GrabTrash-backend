package com.capstone.GrabTrash.controller;

import com.capstone.GrabTrash.dto.PrivateEntityUpdateRequest;
import com.capstone.GrabTrash.service.PrivateEntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/private-entities")
public class PrivateEntityController {

    private final PrivateEntityService privateEntityService;

    @Autowired
    public PrivateEntityController(PrivateEntityService privateEntityService) {
        this.privateEntityService = privateEntityService;
    }

    /**
     * Update private entity information
     */
    @PutMapping("/{userId}")
    public ResponseEntity<?> updatePrivateEntity(
            @PathVariable String userId,
            @RequestBody PrivateEntityUpdateRequest request) {
        return privateEntityService.updatePrivateEntity(userId, request);
    }

    /**
     * Get private entity information
     */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getPrivateEntity(@PathVariable String userId) {
        return privateEntityService.getPrivateEntity(userId);
    }

    /**
     * Get all private entities (admin only)
     */
    @GetMapping
    public ResponseEntity<?> getAllPrivateEntities() {
        return privateEntityService.getAllPrivateEntities();
    }
} 