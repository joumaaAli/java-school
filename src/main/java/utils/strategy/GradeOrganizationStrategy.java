package utils.strategy;

import model.subject.TestResult;
import java.util.List;
import java.util.Map;

/**
 * Strategy interface for organizing a list of test results.
 */
public interface GradeOrganizationStrategy {
    Map<String, List<TestResult>> organizeGrades(List<TestResult> testResults);
}
