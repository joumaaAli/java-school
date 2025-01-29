package model.subject;

import java.io.Serializable;

public class Chapter implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String subjectId;
    private String description;

    public Chapter(String id, String name, String subjectId, String description) {
        this.id = id;
        this.name = name;
        this.subjectId = subjectId;
        this.description = description;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getDescription() {
        return description;
    }

    // toString for serialization (optional)
    @Override
    public String toString() {
        return "Chapter{" +
                ", name='" + name + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
