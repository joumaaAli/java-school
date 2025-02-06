package utils.interpreter;

import model.subject.Question;

/**
 * Basic grading rule: full credit only if the selected option is exactly
 * correct.
 */
public class BasicGradingRuleExpression implements GradingRuleExpression {
    @Override
    public double evaluate(Question question, int selectedOption) {
        return (selectedOption == question.getCorrectOption()) ? 1.0 : 0.0;
    }
}
