package model.subject;

import model.user.Notification;
import model.user.Student;
import model.user.Teacher;
import model.user.UserStorage;
import utils.SerializationUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

    public Session(String id, String title, String dateTime, String chapterId, String groupId) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.chapterId = chapterId;
        this.groupId = groupId;
        this.messages = new ArrayList<>();
        this.teacherIds = new ArrayList<>();
        this.studentIds = new ArrayList<>();
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
        }
    }

    public void removeStudent(String studentId) {
        studentIds.remove(studentId);
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void notifyStudents(List<Student> students) {
        for (Student s : students) {
            Notification notif = new Notification("New session: " + title, this.id);
            System.out.println("Notifying student: " + s.getId());
            System.out.println(notif);
            s.addNotification(notif);
        }
        SerializationUtil.saveDataToDisk(UserStorage.getUsers(), "users.txt");
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