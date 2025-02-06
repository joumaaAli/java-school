package utils.memento;

import model.subject.Test;
import java.util.ArrayList;
import java.util.List;

/**
 * Memento object that stores the state of a list of tests (exams).
 */
public class ExamMemento {
    private List<Test> state;

    public ExamMemento(List<Test> state) {
        this.state = new ArrayList<>(state);
    }

    public List<Test> getState() {
        return state;
    }
}
