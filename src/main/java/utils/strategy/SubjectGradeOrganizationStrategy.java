package utils.strategy;

import model.subject.TestResult;
import model.subject.Test;
import java.util.*;

/**
 * Organizes test results grouped by subject (here identified by chapterId).
 */
public class SubjectGradeOrganizationStrategy implements GradeOrganizationStrategy {
    private List<Test> tests;

    public SubjectGradeOrganizationStrategy(List<Test> tests) {
        this.tests = tests;
    }

    @Override
    public Map<String, List<TestResult>> organizeGrades(List<TestResult> testResults) {
        Map<String, List<TestResult>> map = new HashMap<>();
        // For each test result, find the corresponding test to extract its chapterId.
        for (TestResult tr : testResults) {
            for (Test t : tests) {
                if (t.getId().equals(tr.getTestId())) {
                    map.computeIfAbsent(t.getChapterId(), k -> new ArrayList<>()).add(tr);
                    break;
                }
            }
        }
        return map;
    }
}
