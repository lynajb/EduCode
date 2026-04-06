package com.example.educode.models;

public class Question {
    private int id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String correctOption; // Stocke "A", "B" ou "C"
    private String imageName; // Nom de l'image dans drawable (ex: "sign_stop")

    public Question(int id, String questionText, String optionA, String optionB, String optionC, String correctOption, String imageName) {
        this.id = id;
        this.questionText = questionText;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.correctOption = correctOption;
        this.imageName = imageName;
    }

    // Getters
    public String getQuestionText() { return questionText; }
    public String getOptionA() { return optionA; }
    public String getOptionB() { return optionB; }
    public String getOptionC() { return optionC; }
    public String getCorrectOption() { return correctOption; }
    public String getImageName() { return imageName; }
}