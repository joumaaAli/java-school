package utils.memento;

import model.subject.Test;

public class TestOriginator {
    private Test state;

    public void setState(Test state) {
        this.state = state.copy();
    }

    public Test getState() {
        return state.copy();
    }

    public TestMemento saveStateToMemento() {
        return new TestMemento(state);
    }

    public void restoreState(TestMemento memento) {
        state = memento.getState().copy();
    }
}
