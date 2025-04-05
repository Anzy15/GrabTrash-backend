package com.capstone.GrabTrash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ForgotPasswordRequest {
    @JsonProperty("identifier")
    private String identifier; // Can be email or username

    @JsonProperty("answers")
    private List<SecurityQuestionRequest> answers;

    @JsonProperty("newPassword")
    private String newPassword;

    public ForgotPasswordRequest() {}

    public ForgotPasswordRequest(String identifier, List<SecurityQuestionRequest> answers, String newPassword) {
        this.identifier = identifier;
        this.answers = answers;
        this.newPassword = newPassword;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public List<SecurityQuestionRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<SecurityQuestionRequest> answers) {
        this.answers = answers;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
} 