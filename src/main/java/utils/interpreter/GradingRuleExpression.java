package utils.interpreter;

import model.subject.Question;

/**
 * Expression interface to evaluate a grading rule.
 */
public interface GradingRuleExpression {
    double evaluate(Question question, int selectedOption);
}
