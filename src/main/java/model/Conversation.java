package model;

import java.sql.Timestamp;

public class Conversation {
    private int conversationId;
    private int buyerId;
    private int supplierId;
    private Integer orderId; // Wrapper class to handle NULL values
    private Timestamp createdAt;

    // Default Constructor
    public Conversation() {}

    // Constructor for creating a new conversation without an order
    public Conversation(int buyerId, int supplierId) {
        this.buyerId = buyerId;
        this.supplierId = supplierId;
    }

    // Constructor for creating a conversation related to a specific order
    public Conversation(int buyerId, int supplierId, Integer orderId) {
        this.buyerId = buyerId;
        this.supplierId = supplierId;
        this.orderId = orderId;
    }

    // Full Constructor
    public Conversation(int conversationId, int buyerId, int supplierId, Integer orderId, Timestamp createdAt) {
        this.conversationId = conversationId;
        this.buyerId = buyerId;
        this.supplierId = supplierId;
        this.orderId = orderId;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getConversationId() { return conversationId; }
    public void setConversationId(int conversationId) { this.conversationId = conversationId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}