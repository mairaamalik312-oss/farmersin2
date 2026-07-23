package model;

import java.sql.Timestamp;

public class Review {
    private int reviewId;
    private int orderId;
    private int buyerId;
    private int supplierId;
    private int rating; // 1 to 5
    private String comments;
    private Timestamp createdAt;

    // Default Constructor
    public Review() {}

    // Constructor for submitting a new review
    public Review(int orderId, int buyerId, int supplierId, int rating, String comments) {
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.supplierId = supplierId;
        this.rating = rating;
        this.comments = comments;
    }

    // Full Constructor
    public Review(int reviewId, int orderId, int buyerId, int supplierId,
                  int rating, String comments, Timestamp createdAt) {
        this.reviewId = reviewId;
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.supplierId = supplierId;
        this.rating = rating;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getReviewId() { return reviewId; }
    public void setReviewId(int reviewId) { this.reviewId = reviewId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
