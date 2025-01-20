package model.subject;

public class Material {
    private String id;
    private String title;
    private String type;
    private String pathOrContent;
    private String chapterId;

    public Material(String id, String title, String type, String pathOrContent, String chapterId) {
        this.id = id;
        this.title = title;
        this.type = type;
        this.pathOrContent = pathOrContent;
        this.chapterId = chapterId;
    }

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

    public String toStringCSV() {
        // For CSV writing
        // Format: "id,title,type,pathOrContent,chapterId"
        return id + "," + title + "," + type + "," + pathOrContent + "," + chapterId;
    }

    @Override
    public String toString() {
        // For display in UI components
        return title + " (" + type + ") - " + pathOrContent;
    }
}
