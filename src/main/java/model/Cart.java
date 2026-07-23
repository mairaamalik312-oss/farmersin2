package model;

import java.sql.Timestamp;

public class Cart {
    private int cartId;
    private int buyerId;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default Constructor
    public Cart() {}

    // Constructor for creating a new cart
    public Cart(int buyerId) {
        this.buyerId = buyerId;
    }

    // Full Constructor
    public Cart(int cartId, int buyerId, Timestamp createdAt, Timestamp updatedAt) {
        this.cartId = cartId;
        this.buyerId = buyerId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}