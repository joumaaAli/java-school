package model.subject;

public class Chapter {
    private String id;
    private String subjectId;
    private String name;
    private String objectives;

    public Chapter(String id, String subjectId, String name, String objectives) {
        this.id = id;
        this.subjectId = subjectId;
        this.name = name;
        this.objectives = objectives;
    }

    public String getId() {
        return id;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getName() {
        return name;
    }

    public String getObjectives() {
        return objectives;
    }

    public String toStringCSV() {
        // For CSV writing
        // Format: "id,subjectId,name,Objectives"
        return id + "," + subjectId + "," + name + "," + objectives;
    }

    public String toStringForDisplay() {
        return name + " - Objectives: " + objectives;
    }

    @Override
    public String toString() {
        // For display in JComboBox and other UI components
        return name;
    }
}
