package utils.memento;

import model.subject.Test;
import java.util.List;

/**
 * The originator that creates a memento of its current state (here, the list of
 * tests).
 */
public class ExamOriginator {
    private List<Test> tests;

    public void setState(List<Test> tests) {
        this.tests = tests;
    }

    public List<Test> getState() {
        return tests;
    }

    public ExamMemento saveStateToMemento() {
        return new ExamMemento(tests);
    }

    public void getStateFromMemento(ExamMemento memento) {
        tests = memento.getState();
    }
}
