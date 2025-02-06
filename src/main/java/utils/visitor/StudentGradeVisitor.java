package utils.visitor;

import model.subject.Test;
import model.subject.TestResult;

/**
 * Visitor that extracts grade information for a specific student.
 */
public class StudentGradeVisitor implements ExamVisitor {
    private String studentId;
    private double totalScore = 0;
    private int examCount = 0;

    public StudentGradeVisitor(String studentId) {
        this.studentId = studentId;
    }

    @Override
    public void visit(Test test) {
        for (TestResult tr : test.getTestResults()) {
            if (tr.getStudentId().equals(studentId)) {
                totalScore += tr.getScore();
                examCount++;
            }
        }
    }

    public double getAverageGrade() {
        return (examCount == 0) ? 0 : totalScore / examCount;
    }
}
