package model.subject;

public class Question {
    private String id;
    private String testId;
    private String text;
    private String[] options;
    private int correctOption; // 0-3

    public Question(String id, String testId, String text, String[] options, int correctOption) {
        this.id = id;
        this.testId = testId;
        this.text = text;
        this.options = options;
        this.correctOption = correctOption;
    }

    public String getId() {
        return id;
    }

    public String getTestId() {
        return testId;
    }

    public String getText() {
        return text;
    }

    public String[] getOptions() {
        return options;
    }

    public int getCorrectOption() {
        return correctOption;
    }

    public String toStringCSV() {
        // For CSV writing
        // Format: "id,text,option1|option2|option3|option4,correctOption"
        return id + "," + text + "," + String.join("|", options) + "," + correctOption;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(text).append("\n");
        for (int i = 0; i < options.length; i++) {
            sb.append((i + 1)).append(". ").append(options[i]).append("\n");
        }
        return sb.toString();
    }
}
