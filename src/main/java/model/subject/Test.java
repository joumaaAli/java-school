package model.subject;

import java.util.ArrayList;
import java.util.List;

public class Test {
    private String id;
    private String chapterId;
    private String title;
    private String startTime;
    private int duration;
    private List<Question> questions;

    public Test(String id, String chapterId, String title, String startTime, int duration) {
        this.id = id;
        this.chapterId = chapterId;
        this.title = title;
        this.startTime = startTime;
        this.duration = duration;
        this.questions = new ArrayList<>();
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public List<Question> getQuestions() {
        return questions;
    }

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

    public String toStringCSV() {
        // For CSV writing
        // Format: "id,chapterId,title,startTime,duration,questions"
        return this.getId() + "," + this.getChapterId() + "," + this.getTitle() + "," + this.getStartTime() + ","
                + this.getDuration() + "," + questionsToString();
    }

    private String questionsToString() {
        StringBuilder sb = new StringBuilder();
        for (Question q : questions) {
            sb.append(q.toStringCSV()).append(";");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        // For display in UI components
        return this.getTitle() + " (Start: " + this.getStartTime() + ", Duration: " + this.getDuration() + " mins)";
    }
}
