package utils.decorator;

import model.subject.Question;
import utils.interpreter.GradingRuleExpression;

/**
 * Extends a grading rule by awarding partial credit if the selected answer is
 * adjacent to the correct answer.
 */
public class OrderedMultipleChoiceDecorator extends GradingRuleDecorator {
    public OrderedMultipleChoiceDecorator(GradingRuleExpression wrappee) {
        super(wrappee);
    }

    @Override
    public double evaluate(Question question, int selectedOption) {
        double baseScore = wrappee.evaluate(question, selectedOption);
        if (baseScore == 0.0) {
            int correct = question.getCorrectOption();
            if (Math.abs(selectedOption - correct) == 1) {
                return 0.5; // Award partial credit.
            }
        }
        return baseScore;
    }
}
