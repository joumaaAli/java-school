package model.subject;

import java.io.Serializable;
import java.util.ArrayList;

public class Test implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String chapterId;
    private String title;
    private String startTime;
    private int duration; // in minutes
    private ArrayList<Question> questions;

    public Test(String id, String chapterId, String title, String startTime, int duration) {
        this.id = id;
        this.chapterId = chapterId;
        this.title = title;
        this.startTime = startTime;
        this.duration = duration;
        this.questions = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getChapterId() {
        return chapterId;
    }

    public String getTitle() {
        return title;
    }

    public String getStartTime() {
        return startTime;
    }

    public int getDuration() {
        return duration;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    // Methods to manage questions
    public void addQuestion(Question question) {
        questions.add(question);
    }

    // toString for serialization (optional)
    @Override
    public String toString() {
        return "Test{" +
                "id='" + id + '\'' +
                ", chapterId='" + chapterId + '\'' +
                ", title='" + title + '\'' +
                ", startTime='" + startTime + '\'' +
                ", duration=" + duration +
                ", questions=" + questions +
                '}';
    }
}
