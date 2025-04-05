package com.capstone.GrabTrash.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SecurityQuestionRequest {
    @JsonProperty("identifier")
    private String identifier; // Can be email or username

    @JsonProperty("questionId")
    private String questionId;

    @JsonProperty("answer")
    private String answer;

    public SecurityQuestionRequest() {}

    public SecurityQuestionRequest(String identifier, String questionId, String answer) {
        this.identifier = identifier;
        this.questionId = questionId;
        this.answer = answer;
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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
} 