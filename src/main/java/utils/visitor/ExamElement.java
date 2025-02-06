package utils.visitor;

public interface ExamElement {
    void accept(ExamVisitor visitor);
}