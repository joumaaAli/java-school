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
    private ArrayList<TestResult> testResults; // To store exam results

    public Test(String id, String chapterId, String title, String startTime, int duration) {
        this.id = id;
        this.chapterId = chapterId;
        this.title = title;
        this.startTime = startTime;
        this.duration = duration;
        this.questions = new ArrayList<>();
        this.testResults = new ArrayList<>();
    }

    public Test(Test other) {
        this.id = other.id;
        this.chapterId = other.chapterId;
        this.title = other.title;
        this.startTime = other.startTime;
        this.duration = other.duration;
        this.questions = new ArrayList<>(other.questions); // shallow copy (assumes Question is not edited here)
        this.testResults = new ArrayList<>(other.testResults);
    }

    public Test copy() {
        return new Test(this);
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ArrayList<Question> getQuestions() {
        return questions;
    }

    public ArrayList<TestResult> getTestResults() {
        return testResults;
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public void addTestResult(TestResult result) {
        if (testResults == null) {
            testResults = new ArrayList<>();
        }
        testResults.add(result);
    }

    // Accept method for the Visitor pattern (if needed)
    public void accept(utils.visitor.ExamVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return title;
    }
}
