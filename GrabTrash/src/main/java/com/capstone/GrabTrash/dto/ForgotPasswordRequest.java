package com.capstone.GrabTrash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ForgotPasswordRequest {
    @JsonProperty("identifier")
    private String identifier; // Can be email or username
    
    @JsonProperty("questionId")
    private String questionId;
    
    @JsonProperty("securityAnswer")
    private String securityAnswer;
    
    @JsonProperty("newPassword")
    private String newPassword;

    public ForgotPasswordRequest() {}

    public ForgotPasswordRequest(String identifier, String questionId, String securityAnswer, String newPassword) {
        this.identifier = identifier;
        this.questionId = questionId;
        this.securityAnswer = securityAnswer;
        this.newPassword = newPassword;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityAnswer(String securityAnswer) {
        this.securityAnswer = securityAnswer;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
} 