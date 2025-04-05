package com.capstone.GrabTrash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class RegisterRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;
    
    @JsonProperty("username")
    private String username;
    
    @JsonProperty("firstName")
    private String firstName;
    
    @JsonProperty("lastName")
    private String lastName;
    
    @JsonProperty("securityQuestions")
    private List<SecurityQuestionRequest> securityQuestions;
    
    @JsonProperty("role")
    private String role;
    
    @JsonProperty("phoneNumber")
    private String phoneNumber;

    public RegisterRequest() {}

    public RegisterRequest(String email, String password, String username, String firstName, String lastName, 
                          List<SecurityQuestionRequest> securityQuestions, String role, String phoneNumber) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.securityQuestions = securityQuestions;
        this.role = role;
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public List<SecurityQuestionRequest> getSecurityQuestions() {
        return securityQuestions;
    }

    public void setSecurityQuestions(List<SecurityQuestionRequest> securityQuestions) {
        this.securityQuestions = securityQuestions;
    }
    
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
