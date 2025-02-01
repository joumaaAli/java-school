package model.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import model.subject.TestResult;

public class Student extends User implements Serializable {
    private static final long serialVersionUID = 1L;
    private double balance;
    private List<String> groupIds;
    private List<Notification> notifications;
    private List<TestResult> testResults;

    public Student(String id, String name, String password, double balance) {
        super(id, name, password);
        this.balance = balance;
        this.groupIds = new ArrayList<>();
        this.notifications = new ArrayList<>();
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

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void addNotification(Notification notification) {
        if (this.notifications == null) {
            this.notifications = new ArrayList<>();
        }
        this.notifications.add(notification);
    }

    public void clearNotifications() {
        this.notifications.clear();
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
