// File: model/user/Notification.java
package model.user;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;
    private String message;
    private LocalDateTime timestamp;
    private String sessionId;

    public Notification(String message, String sessionId) {
        this.message = message;
        this.sessionId = sessionId;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    // toString for display purposes
    @Override
    public String toString() {
        return "[" + timestamp.toString() + "] " + message;
    }
}
