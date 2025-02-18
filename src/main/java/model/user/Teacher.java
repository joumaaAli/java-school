package model.user;

import java.io.Serializable;
import model.subject.*;
import utils.IDGenerator;
import utils.observer.SessionObserver;

import javax.swing.JOptionPane;

public class Teacher extends User implements Serializable, SessionObserver {
    private static final long serialVersionUID = 1L;

    public Teacher(String id, String name, String password) {
        super(id, name, password);
    }

    public Material createMaterial(String title, String type, String pathOrContent, String chapterId) {
        String materialId = IDGenerator.generateID();
        return new Material(materialId, title, type, pathOrContent, chapterId);
    }

    public String getRole() {
        return "Teacher";
    }

    public Test createTest(String chapterId, String title, String startTime, int duration) {
        String testId = IDGenerator.generateID();
        return new Test(testId, chapterId, title, startTime, duration);
    }

    public Question createQuestion(String testId, String text, String[] options, int correctOption) {
        String questionId = IDGenerator.generateID();
        return new Question(questionId, testId, text, options, correctOption);
    }

    public Session createSession(String title, String dateTime, String chapterId, String groupId) {
        String sessionId = IDGenerator.generateID();
        return new Session(sessionId, title, dateTime, chapterId, groupId);
    }

    public void assignToGroup(Group group) {
        group.addTeacher(this.getId());
    }

    @Override
    public void update(Session session, String message) {
        System.out.println("Notification for Teacher " + getName() + ": " + message);
        JOptionPane.showMessageDialog(null, "Session Notification: " + message);
    }
}
