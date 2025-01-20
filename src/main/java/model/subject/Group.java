package model.subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class Group {
    private String id;
    private String groupName;
    private String chapterId;
    private List<String> teacherIds; // Changed from single teacherId to list
    private List<String> studentIds;

    public Group(String id, String groupName, String chapterId, List<String> teacherIds) {
        this.id = id;
        this.groupName = groupName;
        this.chapterId = chapterId;
        this.teacherIds = (teacherIds != null) ? new ArrayList<>(teacherIds) : new ArrayList<>();
        this.studentIds = new ArrayList<>();
    }

    // Overloaded constructor for single teacher assignment (for backward
    // compatibility)
    public Group(String id, String groupName, String chapterId, String teacherId) {
        this(id, groupName, chapterId, teacherId != null && !teacherId.isEmpty() ? Arrays.asList(teacherId) : null);
    }

    public void addTeacher(String teacherId) {
        if (!teacherIds.contains(teacherId)) {
            teacherIds.add(teacherId);
        }
    }

    public void removeTeacher(String teacherId) {
        teacherIds.remove(teacherId);
    }

    public List<String> getTeacherIds() {
        return teacherIds;
    }

    public void setTeacherIds(List<String> teacherIds) {
        this.teacherIds = teacherIds;
    }

    public void addStudent(String studentId) {
        if (!studentIds.contains(studentId)) {
            studentIds.add(studentId);
        }
    }

    public void removeStudent(String studentId) {
        studentIds.remove(studentId);
    }

    public List<String> getStudentIds() {
        return studentIds;
    }

    public String getId() {
        return id;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getChapterId() {
        return chapterId;
    }

    public void setChapterId(String chapterId) {
        this.chapterId = chapterId;
    }

    /**
     * Converts the group data to a CSV-formatted string.
     * Format: id,groupName,chapterId,teacherIds (semicolon-separated),studentIds
     * (semicolon-separated)
     */
    public String toStringCSV() {
        String teachers = String.join(";", teacherIds);
        String students = String.join(";", studentIds);
        return id + "," + groupName + "," + chapterId + "," + teachers + "," + students;
    }

    @Override
    public String toString() {
        return groupName + " (Enrollees: " + studentIds.size() + ")";
    }
}
