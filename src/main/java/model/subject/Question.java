package model.subject;

import java.io.Serializable;

public class Question implements Serializable {
    private String id;
    private String testId;
    private String text;
    private String[] options;
    private int correctOption; // 0-based index

    public Question(String id, String testId, String text, String[] options, int correctOption) {
        this.id = id;
        this.testId = testId;
        this.text = text;
        this.options = options;
        this.correctOption = correctOption;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public String getTestId() {
        return testId;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public String getText() {
        return text;
    }

    public String[] getOptions() {
        return options;
    }

    public int getCorrectOption() {
        return correctOption;
    }

    @Override
    public String toString() {
        return text;
    }
}
