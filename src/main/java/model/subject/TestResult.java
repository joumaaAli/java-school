package model.subject;

import java.io.Serializable;
import java.util.Map;

public class TestResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String testId;
    private String studentId;
    private Map<String, Integer> answers; // Map<QuestionID, SelectedOption>
    private double score;
    private String submissionTime;

    public TestResult(String testId, String studentId, Map<String, Integer> answers, double score,
            String submissionTime) {
        this.testId = testId;
        this.studentId = studentId;
        this.answers = answers;
        this.score = score;
        this.submissionTime = submissionTime;
    }

    // Getters and Setters
    public String getTestId() {
        return testId;
    }

    public String getStudentId() {
        return studentId;
    }

    public Map<String, Integer> getAnswers() {
        return answers;
    }

    public double getScore() {
        return score;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }

    public void setTestId(String testId) {
        this.testId = testId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public void setAnswers(Map<String, Integer> answers) {
        this.answers = answers;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    @Override
    public String toString() {
        return "TestResult{" +
                "testId='" + testId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", score=" + score +
                ", submissionTime='" + submissionTime + '\'' +
                '}';
    }
}
