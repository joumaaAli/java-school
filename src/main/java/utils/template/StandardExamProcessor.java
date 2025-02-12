package utils.template;

import model.subject.Test;
import model.subject.TestResult;
import model.subject.Question;
import utils.interpreter.BasicGradingRuleExpression;
import utils.decorator.OrderedMultipleChoiceDecorator;
import utils.interpreter.GradingRuleExpression;
import utils.SerializationUtil;
import java.util.List;

public class StandardExamProcessor extends ExamProcessor {

    private GradingRuleExpression gradingRule;

    public StandardExamProcessor() {
        GradingRuleExpression basicRule = new BasicGradingRuleExpression();
        this.gradingRule = new OrderedMultipleChoiceDecorator(basicRule);
    }

    @Override
    protected void prepareExam(Test test) {
        if (test.getQuestions() == null || test.getQuestions().isEmpty()) {
            throw new IllegalStateException("Exam '" + test.getTitle() + "' has no questions and cannot be processed.");
        }
        System.out
                .println("Exam '" + test.getTitle() + "' prepared with " + test.getQuestions().size() + " questions.");
    }

    @Override
    protected void conductExam(Test test) {
        List<TestResult> results = test.getTestResults();
        if (results.isEmpty()) {
            System.out.println("Warning: Exam '" + test.getTitle() + "' has no submissions yet.");
        } else {
            System.out.println("Exam '" + test.getTitle() + "' conducted with " + results.size() + " submissions.");
        }
    }

    @Override
    protected void gradeExam(Test test) {
        List<TestResult> results = test.getTestResults();
        for (TestResult tr : results) {
            double totalScore = 0.0;
            List<Question> questions = test.getQuestions();
            for (Question q : questions) {
                Integer selectedOption = tr.getAnswers().get(q.getId());
                double scoreForQuestion = (selectedOption != null) ? gradingRule.evaluate(q, selectedOption) : 0.0;
                totalScore += scoreForQuestion;
            }
            double percentage = (questions.size() > 0) ? (totalScore / questions.size()) * 100.0 : 0.0;
            tr.setScore(percentage);
            System.out.println("Graded submission for student " + tr.getStudentId() + ": "
                    + String.format("%.2f", percentage) + "%");
        }
    }

    @Override
    protected void finalizeExam(Test test) {
        List<TestResult> results = test.getTestResults();
        double sum = 0.0;
        for (TestResult tr : results) {
            sum += tr.getScore();
        }
        double avg = (results.size() > 0) ? sum / results.size() : 0.0;
        System.out.println(
                "Finalized exam '" + test.getTitle() + "'. Overall average score: " + String.format("%.2f", avg) + "%");
        SerializationUtil.saveDataToDisk(test, "test_" + test.getId() + ".txt");
    }
}
