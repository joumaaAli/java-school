package utils.memento;

import model.subject.Test;

public class TestMemento {
    private final Test state;

    public TestMemento(Test state) {
        this.state = state.copy();
    }

    public Test getState() {
        return state;
    }
}
