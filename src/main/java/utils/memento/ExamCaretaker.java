package utils.memento;

import java.util.ArrayList;
import java.util.List;

/**
 * Caretaker that keeps track of exam mementos.
 */
public class ExamCaretaker {
    private List<ExamMemento> mementoList = new ArrayList<>();

    public void addMemento(ExamMemento memento) {
        mementoList.add(memento);
    }

    public ExamMemento getMemento(int index) {
        return mementoList.get(index);
    }
}
