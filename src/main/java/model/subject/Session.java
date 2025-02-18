package model.subject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import utils.observer.SessionObserver;

public class Session implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String title;
    private String dateTime;
    private String chapterId;
    private String groupId;
    private List<Message> messages;
    private List<String> teacherIds;
    private List<String> studentIds; // Tracks students currently in the session

    private transient List<SessionObserver> observers = new ArrayList<>();

    public Session(String id, String title, String dateTime, String chapterId, String groupId) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.chapterId = chapterId;
        this.groupId = groupId;
        this.messages = new ArrayList<>();
        this.teacherIds = new ArrayList<>();
        this.studentIds = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getChapterId() {
        return chapterId;
    }

    public String getGroupId() {
        return groupId;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public List<String> getTeacherIds() {
        return teacherIds;
    }

    public List<String> getStudentIds() {
        return studentIds;
    }

    public void addTeacher(String teacherId) {
        if (!teacherIds.contains(teacherId)) {
            teacherIds.add(teacherId);
        }
    }

    public void removeTeacher(String teacherId) {
        teacherIds.remove(teacherId);
    }

    public void addStudent(String studentId) {
        if (!studentIds.contains(studentId)) {
            studentIds.add(studentId);
            // Notify observers (the teacher) when a student joins.
            notifyObservers("Student with ID " + studentId + " has joined the session: " + title);
        }
    }

    public void removeStudent(String studentId) {
        studentIds.remove(studentId);
        notifyObservers("Student with ID " + studentId + " has left the session: " + title);
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    // Observer methods
    public void attachObserver(SessionObserver observer) {
        if (observers == null) {
            observers = new ArrayList<>();
        }
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void detachObserver(SessionObserver observer) {
        if (observers != null) {
            observers.remove(observer);
        }
    }

    public void notifyObservers(String message) {
        if (observers != null) {
            for (SessionObserver observer : observers) {
                observer.update(this, message);
            }
        }
    }

    @Override
    public String toString() {
        return "Session{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
