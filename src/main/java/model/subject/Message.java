// File: model/subject/Message.java
package model.subject;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;
    private String senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;

    public Message(String senderId, String senderName, String content) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getSenderId() {
        return senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // toString for display purposes
    @Override
    public String toString() {
        return "[" + timestamp.toString() + "] " + senderName + ": " + content;
    }
}
