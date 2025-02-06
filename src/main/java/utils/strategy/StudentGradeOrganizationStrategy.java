package utils.strategy;

import model.subject.TestResult;
import java.util.*;

/**
 * Organizes test results grouped by student ID.
 */
public class StudentGradeOrganizationStrategy implements GradeOrganizationStrategy {
    @Override
    public Map<String, List<TestResult>> organizeGrades(List<TestResult> testResults) {
        Map<String, List<TestResult>> map = new HashMap<>();
        for (TestResult tr : testResults) {
            map.computeIfAbsent(tr.getStudentId(), k -> new ArrayList<>()).add(tr);
        }
        return map;
    }
}
