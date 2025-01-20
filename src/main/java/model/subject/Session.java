package model.subject;

import model.user.Student;

import java.util.List;

public class Session {
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

    public String toStringCSV() {
        // For CSV writing
        // Format: "id,title,dateTime,chapterId,groupId"
        return id + "," + title + "," + dateTime + "," + chapterId + "," + groupId;
    }

    /**
     * Notifies all students in the group about the new session.
     *
     * @param students List of students to notify.
     */
    public void notifyStudents(List<Student> students) {
        for (Student s : students) {
            // Placeholder for notification logic, e.g., send email or display message
            System.out.println("Notifying student " + s.getName() + " about new session: " + title);
        }
    }

    @Override
    public String toString() {
        // For display in UI components
        return title + " (" + dateTime + ")";
    }
}
