package com.capstone.GrabTrash.model;

import java.util.Date;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.PropertyName;

public class User {
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String oldPassword;
    private String newPassword;
    private String role;
    private Timestamp createdAt;  // Using com.google.cloud.Timestamp
    private String location;
    private UserPreferences preferences;

    // Default constructor
    public User() {}

    // Explicit PropertyName annotations to ensure correct field mapping
    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("username")
    public String getUsername() {
        return username;
    }

    @PropertyName("username")
    public void setUsername(String username) {
        this.username = username;
    }

    @PropertyName("firstName")
    public String getFirstName() {
        return firstName;
    }

    @PropertyName("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @PropertyName("lastName")
    public String getLastName() {
        return lastName;
    }

    @PropertyName("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @PropertyName("email")
    public String getEmail() {
        return email;
    }

    @PropertyName("email")
    public void setEmail(String email) {
        this.email = email;
    }

    @PropertyName("password")
    public String getPassword() {
        return password;
    }

    @PropertyName("password")
    public void setPassword(String password) {
        this.password = password;
    }

    @PropertyName("oldPassword")
    public String getOldPassword() {
        return oldPassword;
    }

    @PropertyName("oldPassword")
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    @PropertyName("newPassword")
    public String getNewPassword() {
        return newPassword;
    }

    @PropertyName("newPassword")
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    @PropertyName("role")
    public String getRole() {
        return role;
    }

    @PropertyName("role")
    public void setRole(String role) {
        this.role = role;
    }

    @PropertyName("createdAt")
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @PropertyName("createdAt")
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @PropertyName("location")
    public String getLocation() {
        return location;
    }

    @PropertyName("location")
    public void setLocation(String location) {
        this.location = location;
    }

    @PropertyName("preferences")
    public UserPreferences getPreferences() {
        return preferences;
    }

    @PropertyName("preferences")
    public void setPreferences(UserPreferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                ", location='" + location + '\'' +
                ", preferences=" + preferences +
                '}';
    }
}
