package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Refund {
    private int refundId;
    private int paymentId;
    private int orderId;
    private BigDecimal refundAmount;
    private String refundReason;
    private String refundStatus; // REQUESTED, APPROVED, REJECTED, COMPLETED
    private Timestamp requestedAt;
    private Timestamp processedAt; // Can be NULL

    // Default Constructor
    public Refund() {}

    // Constructor for submitting a new refund request
    public Refund(int paymentId, int orderId, BigDecimal refundAmount, String refundReason) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundStatus = "REQUESTED";
    }

    // Full Constructor
    public Refund(int refundId, int paymentId, int orderId, BigDecimal refundAmount,
                  String refundReason, String refundStatus, Timestamp requestedAt,
                  Timestamp processedAt) {
        this.refundId = refundId;
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.refundAmount = refundAmount;
        this.refundReason = refundReason;
        this.refundStatus = refundStatus;
        this.requestedAt = requestedAt;
        this.processedAt = processedAt;
    }

    // Getters and Setters
    public int getRefundId() { return refundId; }
    public void setRefundId(int refundId) { this.refundId = refundId; }

    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) { this.refundAmount = refundAmount; }

    public String getRefundReason() { return refundReason; }
    public void setRefundReason(String refundReason) { this.refundReason = refundReason; }

    public String getRefundStatus() { return refundStatus; }
    public void setRefundStatus(String refundStatus) { this.refundStatus = refundStatus; }

    public Timestamp getRequestedAt() { return requestedAt; }
    public void setRequestedAt(Timestamp requestedAt) { this.requestedAt = requestedAt; }

    public Timestamp getProcessedAt() { return processedAt; }
    public void setProcessedAt(Timestamp processedAt) { this.processedAt = processedAt; }
}