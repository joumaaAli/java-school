package model.subject;

import model.user.Student;
import java.io.Serializable;
import java.util.List;

public class Session implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String title;
    private String dateTime;
    private String chapterId;
    private String groupId;

    public Session(String id, String title, String dateTime, String chapterId, String groupId) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.chapterId = chapterId;
        this.groupId = groupId;
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

    // Dummy implementation of notifyStudents
    public void notifyStudents(List<Student> students) {
        for (Student s : students) {
            System.out.println("Notifying " + s.getName() + " about session: " + title);
        }
    }

    // toString for serialization (optional)
    @Override
    public String toString() {
        return "Session{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", dateTime='" + dateTime + '\'' +
                ", chapterId='" + chapterId + '\'' +
                ", groupId='" + groupId + '\'' +
                '}';
    }
}
