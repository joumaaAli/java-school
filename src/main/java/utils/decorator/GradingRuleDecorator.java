package utils.decorator;

import utils.interpreter.GradingRuleExpression;

/**
 * Abstract decorator class for grading rules.
 */
public abstract class GradingRuleDecorator implements GradingRuleExpression {
    protected GradingRuleExpression wrappee;

    public GradingRuleDecorator(GradingRuleExpression wrappee) {
        this.wrappee = wrappee;
    }
}
