package utils.template;

import model.subject.Test;

/**
 * Abstract class defining the template method for exam processing.
 */
public abstract class ExamProcessor {
    public final void processExam(Test test) {
        prepareExam(test);
        conductExam(test);
        gradeExam(test);
        finalizeExam(test);
    }

    protected abstract void prepareExam(Test test);

    protected abstract void conductExam(Test test);

    protected abstract void gradeExam(Test test);

    protected abstract void finalizeExam(Test test);
}
