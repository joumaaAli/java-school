package utils.observer;

import model.subject.Session;

public interface SessionObserver {
    void update(Session session, String message);
}
