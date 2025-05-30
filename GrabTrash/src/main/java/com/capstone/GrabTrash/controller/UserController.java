package com.capstone.GrabTrash.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.capstone.GrabTrash.model.User;
import com.capstone.GrabTrash.dto.PasswordUpdateRequest;
import com.capstone.GrabTrash.dto.ForgotPasswordRequest;
import com.capstone.GrabTrash.dto.SecurityQuestionRequest;
import com.capstone.GrabTrash.dto.RegisterRequest;
import com.capstone.GrabTrash.dto.LocationUpdateRequest;
import com.capstone.GrabTrash.service.UserService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        return userService.registerUser(request);
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody User user) {
        return userService.loginUser(user);
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable String userId) {
        return userService.getUserProfile(userId);
    }

    @PutMapping("/profile/{userId}")
    public ResponseEntity<?> updateUserProfile(@PathVariable String userId, @RequestBody User user) {
        return userService.updateUserProfile(userId, user);
    }

    @PutMapping("/profile/{userId}/password")
    public ResponseEntity<?> updatePassword(@PathVariable String userId, @RequestBody PasswordUpdateRequest request) {
        return userService.updatePassword(userId, request);
    }

    @GetMapping("/security-questions")
    public ResponseEntity<?> getSecurityQuestions() {
        return userService.getSecurityQuestions();
    }

    @PostMapping("/forgot-password/question")
    public ResponseEntity<?> getSecurityQuestion(@RequestBody SecurityQuestionRequest request) {
        return userService.getSecurityQuestions(request);
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        return userService.forgotPassword(request);
    }

    @PutMapping("/profile/{userId}/email")
    public ResponseEntity<?> updateEmail(@PathVariable String userId, @RequestBody Map<String, String> request) {
        try {
            userService.updateEmail(userId, request.get("newEmail"));
            Map<String, String> response = new HashMap<>();
            response.put("message", "Email updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/profile/{userId}/collection-stats")
    public ResponseEntity<?> getCollectionStats(@PathVariable String userId) {
        return userService.getCollectionStats(userId);
    }

    @GetMapping("/profile/security-questions")
    public ResponseEntity<?> getUserSecurityQuestions() {
        return userService.getUserSecurityQuestions();
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/total-active")
    public ResponseEntity<?> getTotalActiveUsers() {
        return userService.getTotalActiveUsers();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable String userId) {
        ResponseEntity<?> response = userService.deleteUser(userId);
        if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Access denied. Only admin users can delete accounts.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        }
        return response;
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable String userId, @RequestBody Map<String, String> request) {
        return userService.updateUserRole(userId, request.get("role"));
    }

    /**
     * Update private entity user location (admin only)
     */
    @PutMapping("/location")
    public ResponseEntity<?> updateUserLocation(@RequestBody LocationUpdateRequest request) {
        return userService.updateUserLocation(request);
    }

    /**
     * Get private entity user location (admin or private entity)
     */
    @GetMapping("/location/{userId}")
    public ResponseEntity<?> getUserLocation(@PathVariable String userId) {
        return userService.getUserLocation(userId);
    }

    /**
     * Get locations of all private entity users (admin only)
     */
    @GetMapping("/locations/private-entities")
    public ResponseEntity<?> getAllPrivateEntityLocations() {
        return userService.getAllPrivateEntityLocations();
    }
}