package com.capstone.GrabTrash.model;

import java.util.Date;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.PropertyName;
import java.util.List;
import java.util.ArrayList;

public class User {
    private String userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String role;
    private Timestamp createdAt;  // Using com.google.cloud.Timestamp
    private String location;
    private UserPreferences preferences;
    private List<SecurityQuestionAnswer> securityQuestions;
    private String phoneNumber;

    // Default constructor
    public User() {
        this.securityQuestions = new ArrayList<>();
    }

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
    
    @PropertyName("securityQuestions")
    public List<SecurityQuestionAnswer> getSecurityQuestions() {
        return securityQuestions;
    }

    @PropertyName("securityQuestions")
    public void setSecurityQuestions(List<SecurityQuestionAnswer> securityQuestions) {
        this.securityQuestions = securityQuestions;
    }

    @PropertyName("phoneNumber")
    public String getPhoneNumber() {
        return phoneNumber;
    }

    @PropertyName("phoneNumber")
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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
