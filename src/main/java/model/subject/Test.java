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
    private ArrayList<TestResult> testResults; // New field to store exam results

    public Test(String id, String chapterId, String title, String startTime, int duration) {
        this.id = id;
        this.chapterId = chapterId;
        this.title = title;
        this.startTime = startTime;
        this.duration = duration;
        this.questions = new ArrayList<>();
        this.testResults = new ArrayList<>();
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

    public ArrayList<TestResult> getTestResults() {
        return testResults;
    }

    public void addTestResult(TestResult result) {
        if (testResults == null) {
            testResults = new ArrayList<>();
        }
        testResults.add(result);
    }

    // Methods to manage questions
    public void addQuestion(Question question) {
        questions.add(question);
    }

    // Accept method for the Visitor pattern
    public void accept(utils.visitor.ExamVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return "Test{" +
                title + '\'' +
                '}';
    }
}
