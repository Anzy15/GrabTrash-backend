package com.capstone.GrabTrash.dto;

import com.capstone.GrabTrash.model.SecurityQuestion;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class SecurityQuestionsList {
    @JsonProperty("questions")
    private List<SecurityQuestionItem> questions;

    public SecurityQuestionsList() {
        this.questions = new ArrayList<>();
        for (SecurityQuestion question : SecurityQuestion.values()) {
            this.questions.add(new SecurityQuestionItem(question.name(), question.getQuestionText()));
        }
    }

    public List<SecurityQuestionItem> getQuestions() {
        return questions;
    }

    public void setQuestions(List<SecurityQuestionItem> questions) {
        this.questions = questions;
    }

    public static class SecurityQuestionItem {
        @JsonProperty("id")
        private String id;

        @JsonProperty("text")
        private String text;

        public SecurityQuestionItem() {}

        public SecurityQuestionItem(String id, String text) {
            this.id = id;
            this.text = text;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
} 