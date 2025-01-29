package model.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import model.subject.TestResult;

public class Student extends User implements Serializable {
    private double balance;
    private List<String> groupIds;
    private List<TestResult> testResults; // New field

    public Student(String id, String name, String password, double balance) {
        super(id, name, password);
        this.balance = balance;
        this.groupIds = new ArrayList<>();
        this.testResults = new ArrayList<>();
    }

    // Getters and Setters

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public void enrollGroup(String groupId) {
        if (!groupIds.contains(groupId)) {
            groupIds.add(groupId);
        }
    }

    public void leaveGroup(String groupId) {
        groupIds.remove(groupId);
    }

    public List<TestResult> getTestResults() {
        return testResults;
    }

    public void addTestResult(TestResult result) {
        testResults.add(result);
    }

    @Override
    public String getRole() {
        return "Student";
    }
}
