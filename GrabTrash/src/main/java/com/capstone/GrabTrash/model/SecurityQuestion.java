package com.capstone.GrabTrash.model;

public enum SecurityQuestion {
    MOTHERS_MAIDEN_NAME("What is your mother's maiden name?"),
    FIRST_PET_NAME("What was the name of your first pet?"),
    BIRTH_CITY("In what city were you born?"),
    FAVORITE_TEACHER("What was the name of your favorite teacher?"),
    CHILDHOOD_STREET("What was the name of the street you grew up on?"),
    FAVORITE_BOOK("What is the title of your favorite book?"),
    FAVORITE_MOVIE("What is the title of your favorite movie?"),
    FAVORITE_SONG("What is the title of your favorite song?"),
    FAVORITE_FOOD("What is your favorite food?"),
    FAVORITE_COLOR("What is your favorite color?");

    private final String questionText;

    SecurityQuestion(String questionText) {
        this.questionText = questionText;
    }

    public String getQuestionText() {
        return questionText;
    }

    public static SecurityQuestion fromQuestionText(String questionText) {
        for (SecurityQuestion question : SecurityQuestion.values()) {
            if (question.getQuestionText().equals(questionText)) {
                return question;
            }
        }
        return null;
    }
} 