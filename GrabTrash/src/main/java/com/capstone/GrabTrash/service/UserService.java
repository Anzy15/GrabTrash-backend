package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.model.*;
import com.capstone.GrabTrash.dto.PasswordUpdateRequest;
import com.capstone.GrabTrash.dto.ForgotPasswordRequest;
import com.capstone.GrabTrash.dto.SecurityQuestionRequest;
import com.capstone.GrabTrash.dto.RegisterRequest;
import com.capstone.GrabTrash.dto.SecurityQuestionsList;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.cloud.Timestamp;
import com.google.firebase.auth.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class UserService {
    private final Firestore firestore;
    private final FirebaseAuth firebaseAuth;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;
    private final JwtService jwtService;
    private final BarangayService barangayService;

    @Autowired
    public UserService(Firestore firestore, FirebaseAuth firebaseAuth, PasswordEncoder passwordEncoder, AuthService authService, JwtService jwtService, @Lazy BarangayService barangayService) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.jwtService = jwtService;
        this.barangayService = barangayService;
    }
    
   
    /**
     * Register a new user
     */
    public ResponseEntity<?> registerUser(RegisterRequest request) {
        try {
            // Check if user already exists
            Query query = firestore.collection("users")
                .whereEqualTo("email", request.getEmail());
            QuerySnapshot querySnapshot = query.get().get();

            if (!querySnapshot.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User with this email already exists");
                return ResponseEntity.badRequest().body(error);
            }

            // Validate barangay
            if (request.getBarangayId() == null || request.getBarangayId().trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Barangay is required");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if barangay exists and is active
            ResponseEntity<?> barangayResponse = barangayService.getBarangayById(request.getBarangayId());
            if (barangayResponse.getStatusCode() != HttpStatus.OK) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid barangay ID");
                return ResponseEntity.badRequest().body(error);
            }
            Barangay barangay = (Barangay) barangayResponse.getBody();

            // Validate security questions
            if (request.getSecurityQuestions() == null || request.getSecurityQuestions().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "At least one security question is required");
                return ResponseEntity.badRequest().body(error);
            }

            if (request.getSecurityQuestions().size() > 3) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Maximum of 3 security questions allowed");
                return ResponseEntity.badRequest().body(error);
            }

            List<SecurityQuestionAnswer> securityQuestions = new ArrayList<>();
            for (SecurityQuestionRequest sqRequest : request.getSecurityQuestions()) {
                SecurityQuestion securityQuestion = SecurityQuestion.valueOf(sqRequest.getQuestionId());
                if (securityQuestion == null) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "Invalid security question: " + sqRequest.getQuestionId());
                    return ResponseEntity.badRequest().body(error);
                }
                securityQuestions.add(new SecurityQuestionAnswer(
                    sqRequest.getQuestionId(),
                    securityQuestion.getQuestionText(),
                    sqRequest.getAnswer()
                ));
            }

            // Create new user
            User user = new User();
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setUsername(request.getUsername());
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());
            user.setSecurityQuestions(securityQuestions);
            user.setRole(request.getRole() != null ? "ROLE_" + request.getRole().toUpperCase() : "ROLE_USER");
            user.setPhoneNumber(request.getPhoneNumber());
            user.setBarangayId(request.getBarangayId());
            user.setBarangayName(barangay.getName());
            user.setCreatedAt(Timestamp.now());
            user.setFcmToken(null); // Initialize FCM token as null

            // Save user to Firestore
            String userId = firestore.collection("users").document().getId();
            user.setUserId(userId);
            firestore.collection("users").document(userId).set(user).get();

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User registered successfully");
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> loginUser(User user) {
        try {
            System.out.println("Login attempt for email: " + user.getEmail());
            
            // Find user by email
            Query query = firestore.collection("users")
                .whereEqualTo("email", user.getEmail());
            QuerySnapshot querySnapshot = query.get().get();

            if (querySnapshot.isEmpty()) {
                System.out.println("User not found for email: " + user.getEmail());
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            User foundUser = querySnapshot.getDocuments().get(0).toObject(User.class);
            System.out.println("User found, verifying password");

            // Verify password
            if (!passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
                System.out.println("Invalid password for user: " + user.getEmail());
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid password");
                return ResponseEntity.badRequest().body(error);
            }

            System.out.println("Password verified, generating token");
            // Generate JWT token
            String token = jwtService.generateToken(foundUser);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            System.out.println("Login successful for user: " + user.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error during login: " + e.getMessage());
            e.printStackTrace();
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    public ResponseEntity<?> getUserProfile(String userId) {
        try {
            User user = firestore.collection("users").document(userId).get().get().toObject(User.class);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            // Create a profile map without sensitive information
            Map<String, Object> profile = new HashMap<>();
            profile.put("userId", user.getUserId());
            profile.put("username", user.getUsername());
            profile.put("firstName", user.getFirstName());
            profile.put("lastName", user.getLastName());
            profile.put("email", user.getEmail());
            profile.put("location", user.getLocation());
            profile.put("preferences", user.getPreferences());
            profile.put("createdAt", user.getCreatedAt());
            profile.put("phoneNumber", user.getPhoneNumber());

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> updateUserProfile(String userId, User user) {
        try {
            User existingUser = firestore.collection("users").document(userId).get().get().toObject(User.class);
            if (existingUser == null) {
                return ResponseEntity.notFound().build();
            }

            // Check if new email is already in use by another user
            QuerySnapshot emailCheck = firestore.collection("users")
                .whereEqualTo("email", user.getEmail())
                .get()
                .get();

            boolean emailInUseByOtherUser = emailCheck.getDocuments().stream()
                .anyMatch(doc -> !doc.getId().equals(userId));

            if (emailInUseByOtherUser) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Email is already in use by another user");
                return ResponseEntity.badRequest().body(error);
            }

            // Update allowed fields
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setEmail(user.getEmail());
            // Preserve existing FCM token
            existingUser.setFcmToken(existingUser.getFcmToken());

            // Update Firestore
            firestore.collection("users").document(userId).set(existingUser).get();

            // Update Firebase Auth
            try {
                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userId)
                    .setEmail(user.getEmail());
                firebaseAuth.updateUser(request);
            } catch (FirebaseAuthException e) {
                System.err.println("Warning: Could not update Firebase Auth: " + e.getMessage());
                // Don't throw exception here, as Firestore update was successful
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Profile updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> updatePassword(String userId, PasswordUpdateRequest request) {
        try {
            User existingUser = firestore.collection("users").document(userId).get().get().toObject(User.class);
            if (existingUser == null) {
                return ResponseEntity.notFound().build();
            }

            // Verify old password
            if (!passwordEncoder.matches(request.getOldPassword(), existingUser.getPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid old password");
                return ResponseEntity.badRequest().body(error);
            }

            // Update password
            existingUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
            firestore.collection("users").document(userId).set(existingUser).get();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> getCollectionStats(String userId) {
        try {
            // TODO: Implement collection stats retrieval
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalCollections", 0);
            stats.put("lastCollection", null);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public User getUserByEmailOrUsername(String identifier) {
        // Use the injected Firestore instance
        CollectionReference users = firestore.collection("users");

        try {
            // Try fetching user by email
            Query query = users.whereEqualTo("email", identifier).limit(1);
            ApiFuture<QuerySnapshot> future = query.get();
            List<QueryDocumentSnapshot> documents = future.get().getDocuments();
            
            if (!documents.isEmpty()) {
                return documents.get(0).toObject(User.class);
            }

            // If not found by email, search by username
            query = users.whereEqualTo("username", identifier).limit(1);
            future = query.get();
            documents = future.get().getDocuments();
            
            if (!documents.isEmpty()) {
                return documents.get(0).toObject(User.class);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // User not found
    }

    public boolean validatePassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPassword()); // Replace with hashed password validation
    }

    // 🔹 Authenticate User (Login)
    public User authenticateUser(String email) throws ExecutionException, InterruptedException {
        CollectionReference usersRef = firestore.collection("users");
        Query query = usersRef.whereEqualTo("email", email).limit(1);
        ApiFuture<QuerySnapshot> future = query.get();

        QuerySnapshot snapshot = future.get();
        if (!snapshot.isEmpty()) {
            DocumentSnapshot document = snapshot.getDocuments().get(0);
            return document.toObject(User.class);
        }

        return null; // User not found
    }

    //  Get User by ID
    public User getUserById(String userId) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection("users").document(userId);
        DocumentSnapshot document = docRef.get().get();

        if (document.exists()) {
            return document.toObject(User.class);
        }
        return null;
    }
    

    //  Update User
    public void updateUser(String userId, Map<String, Object> updates) {
        firestore.collection("users").document(userId).update(updates);
    }

    // Update User Profile Information
    public void updateProfileInfo(String userId, String firstName, String lastName, String location) throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("firstName", firstName);
        updates.put("lastName", lastName);
        updates.put("location", location);
        firestore.collection("users").document(userId).update(updates).get();
    }

    // Update User Email
    public void updateEmail(String userId, String newEmail) throws Exception {
        try {
            // First check if user exists in Firestore
            DocumentReference userDoc = firestore.collection("users").document(userId);
            DocumentSnapshot snapshot = userDoc.get().get();
            
            if (!snapshot.exists()) {
                throw new Exception("User not found in Firestore");
            }

            User user = snapshot.toObject(User.class);
            if (user == null) {
                throw new Exception("User data is invalid");
            }

            // Check if new email is already in use by another user
            QuerySnapshot emailCheck = firestore.collection("users")
                .whereEqualTo("email", newEmail)
                .get()
                .get();

            boolean emailInUseByOtherUser = emailCheck.getDocuments().stream()
                .anyMatch(doc -> !doc.getId().equals(userId));

            if (emailInUseByOtherUser) {
                throw new Exception("Email is already in use by another user");
            }

            // Update Firestore first
            user.setEmail(newEmail);
            userDoc.set(user).get();

            // Then try to update or create Firebase Auth user
            try {
                // Try to get the user from Firebase Auth
                UserRecord userRecord = firebaseAuth.getUser(userId);
                // If user exists, update their email
                UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userId)
                    .setEmail(newEmail);
                firebaseAuth.updateUser(request);
            } catch (FirebaseAuthException e) {
                if (e.getErrorCode().equals("user-not-found")) {
                    // Create new Firebase Auth user if they don't exist
                    UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                        .setUid(userId)
                        .setEmail(newEmail)
                        .setEmailVerified(false)
                        .setPassword("tempPassword123!@#");
                    
                    try {
                        firebaseAuth.createUser(createRequest);
                        System.out.println("Created new Firebase Auth user for: " + userId);
                    } catch (FirebaseAuthException createError) {
                        System.err.println("Warning: Could not create Firebase Auth user: " + createError.getMessage());
                        // Don't throw exception here, as Firestore update was successful
                    }
                } else {
                    System.err.println("Warning: Could not update Firebase Auth: " + e.getMessage());
                    // Don't throw exception here, as Firestore update was successful
                }
            }

        } catch (Exception e) {
            throw new Exception("Error updating email: " + e.getMessage());
        }
    }

    // Update User Password
    public void updatePassword(String userId, String oldPassword, String newPassword) throws Exception {
        // First, get the user from Firestore
        User user = getUserById(userId);
        if (user == null) {
            throw new RuntimeException("User not found");
        }

        // Verify the old password
        if (!validatePassword(user, oldPassword)) {
            throw new RuntimeException("Invalid old password");
        }

        try {
            // Update password in Firebase Auth
            UserRecord.UpdateRequest request = new UserRecord.UpdateRequest(userId)
                    .setPassword(newPassword);
            firebaseAuth.updateUser(request);

            // Hash the new password
            String hashedPassword = passwordEncoder.encode(newPassword);

            // Update password in Firestore
            Map<String, Object> updates = new HashMap<>();
            updates.put("password", hashedPassword);
            firestore.collection("users").document(userId).update(updates).get();

            System.out.println("Password updated successfully for user: " + userId);
        } catch (FirebaseAuthException e) {
            System.err.println("❌ Firebase Auth error while updating password: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update password in Firebase Auth: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("❌ Error while updating password: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update password: " + e.getMessage(), e);
        }
    }

    // Update User Preferences
    public void updatePreferences(String userId, UserPreferences preferences) throws ExecutionException, InterruptedException {
        Map<String, Object> updates = new HashMap<>();
        updates.put("preferences", preferences);
        firestore.collection("users").document(userId).update(updates).get();
    }

    /**
     * Get all available security questions
     */
    public ResponseEntity<?> getSecurityQuestions() {
        try {
            SecurityQuestionsList questionsList = new SecurityQuestionsList();
            return ResponseEntity.ok(questionsList);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get the security questions for a user
     */
    public ResponseEntity<?> getSecurityQuestions(SecurityQuestionRequest request) {
        try {
            User user = getUserByEmailOrUsername(request.getIdentifier());
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("securityQuestions", user.getSecurityQuestions());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get the logged-in user's security questions and answers
     */
    public ResponseEntity<?> getUserSecurityQuestions() {
        try {
            // Get the current authenticated user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String userEmail = authentication.getName();
            User user = getUserByEmailOrUsername(userEmail);
            
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("securityQuestions", user.getSecurityQuestions());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Reset password using security questions
     */
    public ResponseEntity<?> forgotPassword(ForgotPasswordRequest request) {
        try {
            User user = getUserByEmailOrUsername(request.getIdentifier());
            if (user == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Verify all security answers are correct
            List<SecurityQuestionAnswer> userQuestions = user.getSecurityQuestions();
            List<SecurityQuestionRequest> providedAnswers = request.getAnswers();

            // Check if number of answers matches number of questions
            if (userQuestions.size() != providedAnswers.size()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Please answer all security questions");
                return ResponseEntity.badRequest().body(error);
            }

            // Create a map of question IDs to answers for easier lookup
            Map<String, String> userAnswersMap = new HashMap<>();
            for (SecurityQuestionAnswer sqa : userQuestions) {
                userAnswersMap.put(sqa.getQuestionId(), sqa.getAnswer());
            }

            // Verify each answer
            for (SecurityQuestionRequest answer : providedAnswers) {
                String correctAnswer = userAnswersMap.get(answer.getQuestionId());
                if (correctAnswer == null || !correctAnswer.equalsIgnoreCase(answer.getAnswer())) {
                    Map<String, String> error = new HashMap<>();
                    error.put("error", "One or more security answers are incorrect");
                    return ResponseEntity.badRequest().body(error);
                }
            }

            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            firestore.collection("users").document(user.getUserId()).set(user).get();

            Map<String, String> response = new HashMap<>();
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get all users (admin only)
     */
    public ResponseEntity<?> getAllUsers() {
        try {
            // Get the current authenticated user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Get the current user's email
            String userEmail = authentication.getName();
            User currentUser = getUserByEmailOrUsername(userEmail);
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if the user is an admin
            if (!"ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Get all users from Firestore
            QuerySnapshot querySnapshot = firestore.collection("users").get().get();
            List<User> users = new ArrayList<>();
            
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                User user = document.toObject(User.class);
                // Remove sensitive information
                user.setPassword(null);
                users.add(user);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("users", users);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get total number of active users (admin only)
     */
    public ResponseEntity<?> getTotalActiveUsers() {
        try {
            // Get the current authenticated user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Get the current user's email
            String userEmail = authentication.getName();
            User currentUser = getUserByEmailOrUsername(userEmail);
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if the user has the required role (admin or private_entity)
            String userRole = currentUser.getRole().toLowerCase();
            if (!userRole.equals("admin") && !userRole.equals("private_entity")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin or Private Entity role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Get all users from Firestore
            QuerySnapshot querySnapshot = firestore.collection("users").get().get();
            long totalUsers = querySnapshot.size();

            Map<String, Object> response = new HashMap<>();
            response.put("totalActiveUsers", totalUsers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Delete a user account (admin only)
     */
    public ResponseEntity<?> deleteUser(String userId) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String adminEmail = authentication.getName();
            User adminUser = getUserByEmailOrUsername(adminEmail);
            
            if (adminUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Admin user not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if the user is an admin
            if (!"ROLE_ADMIN".equalsIgnoreCase(adminUser.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Check if the user to be deleted exists
            DocumentSnapshot userDoc = firestore.collection("users").document(userId).get().get();
            if (!userDoc.exists()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.notFound().build();
            }

            // Delete the user from Firebase Auth
            try {
                firebaseAuth.deleteUser(userId);
            } catch (FirebaseAuthException e) {
                System.err.println("Warning: Could not delete user from Firebase Auth: " + e.getMessage());
                // Continue with Firestore deletion even if Firebase Auth deletion fails
            }

            // Delete the user from Firestore
            firestore.collection("users").document(userId).delete().get();

            Map<String, String> response = new HashMap<>();
            response.put("message", "User deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Update user role (admin only)
     */
    public ResponseEntity<?> updateUserRole(String userId, String newRole) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            String adminEmail = authentication.getName();
            User adminUser = getUserByEmailOrUsername(adminEmail);
            
            if (adminUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Admin user not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if the user is an admin
            if (!"ROLE_ADMIN".equalsIgnoreCase(adminUser.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Check if the user to be updated exists
            DocumentSnapshot userDoc = firestore.collection("users").document(userId).get().get();
            if (!userDoc.exists()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.notFound().build();
            }

            // Update the user's role
            User user = userDoc.toObject(User.class);
            user.setRole("ROLE_" + newRole.toUpperCase());
            firestore.collection("users").document(userId).set(user).get();

            Map<String, String> response = new HashMap<>();
            response.put("message", "User role updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get total trash picked up (admin only)
     */
    public ResponseEntity<?> getTotalTrashPickedUp() {
        try {
            // Get the current authenticated user from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not authenticated");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            // Get the current user's email
            String userEmail = authentication.getName();
            User currentUser = getUserByEmailOrUsername(userEmail);
            
            if (currentUser == null) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User not found");
                return ResponseEntity.badRequest().body(error);
            }

            // Check if the user is an admin
            if (!"ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Access denied. Admin role required.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
            }

            // Get all completed pickup requests
            Query query = firestore.collection("pickup_requests")
                .whereEqualTo("status", "COMPLETED");
            QuerySnapshot querySnapshot = query.get().get();
            
            double totalWeight = 0.0;
            int totalRequests = 0;
            
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                PickupRequest pickupRequest = document.toObject(PickupRequest.class);
                if (pickupRequest.getTrashWeight() != null) {
                    totalWeight += pickupRequest.getTrashWeight();
                }
                totalRequests++;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("totalTrashPickedUp", totalWeight);
            response.put("totalCompletedRequests", totalRequests);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * Get user ID by email
     * @param email User email
     * @return User ID
     * @throws ExecutionException If there's an error retrieving the user
     * @throws InterruptedException If the operation is interrupted
     */
    public String getUserIdByEmail(String email) throws ExecutionException, InterruptedException {
        CollectionReference usersCollection = firestore.collection("users");
        Query query = usersCollection.whereEqualTo("email", email).limit(1);
        
        QuerySnapshot snapshot = query.get().get();
        if (snapshot.isEmpty()) {
            throw new RuntimeException("User not found with email: " + email);
        }
        
        User user = snapshot.getDocuments().get(0).toObject(User.class);
        return user.getUserId();
    }
}