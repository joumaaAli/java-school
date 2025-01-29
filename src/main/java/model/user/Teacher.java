package model.user;

import java.io.Serializable;

import model.subject.*;
import utils.IDGenerator;

public class Teacher extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    public Teacher(String id, String name, String password) {
        super(id, name, password);
    }

    /**
     * Creates a new material.
     *
     * @param title         The title of the material.
     * @param type          The type of the material (video, image, pdf).
     * @param pathOrContent The path or URL to the material content.
     * @param chapterId     The ID of the chapter to which the material is assigned.
     * @return A new Material object.
     */
    public Material createMaterial(String title, String type, String pathOrContent, String chapterId) {
        String materialId = IDGenerator.generateID();
        return new Material(materialId, title, type, pathOrContent, chapterId);
    }

    /**
     * Returns the role of the user.
     *
     * @return The role of the user.
     */
    public String getRole() {
        return "Teacher";
    }

    /**
     * Creates a new test.
     *
     * @param chapterId The ID of the chapter to which the test is assigned.
     * @param title     The title of the test.
     * @param startTime The start time of the test.
     * @param duration  The duration of the test in minutes.
     * @return A new Test object.
     */
    public Test createTest(String chapterId, String title, String startTime, int duration) {
        String testId = IDGenerator.generateID();
        return new Test(testId, chapterId, title, startTime, duration);
    }

    /**
     * Creates a new question.
     *
     * @param testId        The ID of the test to which the question belongs.
     * @param text          The text of the question.
     * @param options       An array of four options.
     * @param correctOption The index (0-3) of the correct option.
     * @return A new Question object.
     */
    public Question createQuestion(String testId, String text, String[] options, int correctOption) {
        String questionId = IDGenerator.generateID();
        return new Question(questionId, testId, text, options, correctOption);
    }

    /**
     * Creates a new session.
     *
     * @param title     The title of the session.
     * @param dateTime  The date and time of the session.
     * @param chapterId The ID of the chapter associated with the session.
     * @param groupId   The ID of the group associated with the session.
     * @return A new Session object.
     */
    public Session createSession(String title, String dateTime, String chapterId, String groupId) {
        String sessionId = IDGenerator.generateID();
        return new Session(sessionId, title, dateTime, chapterId, groupId);
    }

    /**
     * Assigns the teacher to a group.
     *
     * @param group The group to assign.
     */
    public void assignToGroup(Group group) {
        group.addTeacher(this.getId());
    }
}
