package model;

import java.sql.Timestamp;

public class Message {
    private int messageId;
    private int conversationId;
    private int senderUserId;
    private String messageText;
    private Timestamp sentAt;
    private boolean isRead;

    // Default Constructor
    public Message() {}

    // Constructor for creating a new message (sentAt and isRead handled by DB defaults)
    public Message(int conversationId, int senderUserId, String messageText) {
        this.conversationId = conversationId;
        this.senderUserId = senderUserId;
        this.messageText = messageText;
        this.isRead = false;
    }

    // Full Constructor
    public Message(int messageId, int conversationId, int senderUserId,
                   String messageText, Timestamp sentAt, boolean isRead) {
        this.messageId = messageId;
        this.conversationId = conversationId;
        this.senderUserId = senderUserId;
        this.messageText = messageText;
        this.sentAt = sentAt;
        this.isRead = isRead;
    }

    // Getters and Setters
    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }

    public int getSenderUserId() { return senderUserId; }
    public void setSenderUserId(int senderUserId) { this.senderUserId = senderUserId; }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) { this.messageText = messageText; }

    public Timestamp getSentAt() { return sentAt; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean isRead) { this.isRead = isRead; }
}