package com.capstone.GrabTrash.model;

import com.google.cloud.firestore.annotation.PropertyName;

public class SecurityQuestionAnswer {
    private String questionId;
    private String questionText;
    private String answer;

    public SecurityQuestionAnswer() {}

    public SecurityQuestionAnswer(String questionId, String questionText, String answer) {
        this.questionId = questionId;
        this.questionText = questionText;
        this.answer = answer;
    }

    @PropertyName("questionId")
    public String getQuestionId() {
        return questionId;
    }

    @PropertyName("questionId")
    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    @PropertyName("questionText")
    public String getQuestionText() {
        return questionText;
    }

    @PropertyName("questionText")
    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    @PropertyName("answer")
    public String getAnswer() {
        return answer;
    }

    @PropertyName("answer")
    public void setAnswer(String answer) {
        this.answer = answer;
    }
} 