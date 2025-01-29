package model.subject;

import java.io.Serializable;

public class Material implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String title;
    private String type; // e.g., video, image, pdf
    private String pathOrContent;
    private String chapterId;

    public Material(String id, String title, String type, String pathOrContent, String chapterId) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.pathOrContent = pathOrContent;
        this.chapterId = chapterId;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getType() {
        return type;
    }

    public String getPathOrContent() {
        return pathOrContent;
    }

    public String getChapterId() {
        return chapterId;
    }

    // toString for serialization (optional)
    @Override
    public String toString() {
        return "Material{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", pathOrContent='" + pathOrContent + '\'' +
                ", chapterId='" + chapterId + '\'' +
                '}';
    }
}
