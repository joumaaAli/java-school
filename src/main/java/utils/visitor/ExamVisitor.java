package utils.visitor;

import model.subject.Test;

public interface ExamVisitor {
    void visit(Test test);
}