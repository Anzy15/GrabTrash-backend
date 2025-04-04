package com.capstone.GrabTrash.service;

import com.capstone.GrabTrash.model.*;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.ResponseEntity;

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

    @Autowired
    public UserService(Firestore firestore, FirebaseAuth firebaseAuth, PasswordEncoder passwordEncoder, AuthService authService, JwtService jwtService) {
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
        this.jwtService = jwtService;
    }
    
   
    public ResponseEntity<?> registerUser(User user) {
        try {
            // Check if user already exists
            Query query = firestore.collection("users")
                .whereEqualTo("email", user.getEmail());
            QuerySnapshot querySnapshot = query.get().get();

            if (!querySnapshot.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "User with this email already exists");
                return ResponseEntity.badRequest().body(error);
            }

            // Hash password
            user.setPassword(passwordEncoder.encode(user.getPassword()));

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

            // Update only allowed fields
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setLocation(user.getLocation());

            firestore.collection("users").document(userId).set(existingUser).get();
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    public ResponseEntity<?> updatePassword(String userId, User user) {
        try {
            User existingUser = firestore.collection("users").document(userId).get().get().toObject(User.class);
            if (existingUser == null) {
                return ResponseEntity.notFound().build();
            }

            // Verify old password
            if (!passwordEncoder.matches(user.getOldPassword(), existingUser.getPassword())) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Invalid old password");
                return ResponseEntity.badRequest().body(error);
            }

            // Update password
            existingUser.setPassword(passwordEncoder.encode(user.getNewPassword()));
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

    //  Delete User
    public void deleteUser(String userId) throws FirebaseAuthException {
        FirebaseAuth.getInstance().deleteUser(userId);
        firestore.collection("users").document(userId).delete();
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
}