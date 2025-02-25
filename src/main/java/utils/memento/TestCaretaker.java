package utils.memento;

import java.util.ArrayList;
import java.util.List;

public class TestCaretaker {
    private final List<TestMemento> undoStack = new ArrayList<>();
    private final List<TestMemento> redoStack = new ArrayList<>();

    public void saveState(TestMemento memento) {
        undoStack.add(memento);
    }

    // Allow undo only if there is more than one state (i.e. a previous state
    // exists)
    public boolean canUndo() {
        return undoStack.size() > 1;
    }

    public TestMemento undo() {
        if (!canUndo())
            return null;
        TestMemento current = undoStack.remove(undoStack.size() - 1);
        redoStack.add(current);
        return undoStack.get(undoStack.size() - 1);
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public TestMemento redo() {
        if (!canRedo())
            return null;
        TestMemento memento = redoStack.remove(redoStack.size() - 1);
        undoStack.add(memento);
        return memento;
    }

    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    public void clearRedo() {
        redoStack.clear();
    }
}
