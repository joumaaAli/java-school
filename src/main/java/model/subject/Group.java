package model.subject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Group implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String groupName;
    private String chapterId;
    private ArrayList<String> teacherIds;
    private ArrayList<String> studentIds;

    public Group(String id, String groupName, String chapterId, List<String> teacherIds) {
        this.id = id;
        this.groupName = groupName;
        this.chapterId = chapterId;
        this.teacherIds = new ArrayList<>(teacherIds);
        this.studentIds = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getChapterId() {
        return chapterId;
    }

    public ArrayList<String> getTeacherIds() {
        return teacherIds;
    }

    public List<String> getStudentIds() {
        return studentIds;
    }

    // Methods to manage teachers
    public void addTeacher(String teacherId) {
        if (!teacherIds.contains(teacherId)) {
            teacherIds.add(teacherId);
        }
    }

    public void removeTeacher(String teacherId) {
        teacherIds.remove(teacherId);
    }

    // Methods to manage students
    public void addStudent(String studentId) {
        if (!studentIds.contains(studentId)) {
            studentIds.add(studentId);
        }
    }

    public void removeStudent(String studentId) {
        studentIds.remove(studentId);
    }

    @Override
    public String toString() {
        return groupName;
    }
}
