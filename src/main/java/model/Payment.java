package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Payment {
    private int paymentId;
    private int orderId;
    private int buyerId;
    private String paymentType; // ADVANCE, REMAINING, REFUND
    private String paymentMethod;
    private BigDecimal amount;
    private String transactionReference;
    private String paymentStatus; // PENDING, SUCCESSFUL, FAILED, REFUNDED
    private Timestamp paymentDate;
    private String proofImagePath;
    private Integer verifiedByAdmin; // Nullable FK

    // Default Constructor
    public Payment() {}

    // Constructor for creating a new payment entry
    public Payment(int orderId, int buyerId, String paymentType, String paymentMethod,
                   BigDecimal amount, String transactionReference, String proofImagePath) {
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.paymentType = paymentType;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.transactionReference = transactionReference;
        this.proofImagePath = proofImagePath;
        this.paymentStatus = "PENDING";
    }

    // Full Constructor
    public Payment(int paymentId, int orderId, int buyerId, String paymentType,
                   String paymentMethod, BigDecimal amount, String transactionReference,
                   String paymentStatus, Timestamp paymentDate, String proofImagePath,
                   Integer verifiedByAdmin) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.paymentType = paymentType;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.transactionReference = transactionReference;
        this.paymentStatus = paymentStatus;
        this.paymentDate = paymentDate;
        this.proofImagePath = proofImagePath;
        this.verifiedByAdmin = verifiedByAdmin;
    }

    // Getters and Setters
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int paymentId) { this.paymentId = paymentId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public String getPaymentType() { return paymentType; }
    public void setPaymentType(String paymentType) { this.paymentType = paymentType; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionReference() { return transactionReference; }
    public void setTransactionReference(String transactionReference) { this.transactionReference = transactionReference; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Timestamp getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }

    public String getProofImagePath() { return proofImagePath; }
    public void setProofImagePath(String proofImagePath) { this.proofImagePath = proofImagePath; }

    public Integer getVerifiedByAdmin() { return verifiedByAdmin; }
    public void setVerifiedByAdmin(Integer verifiedByAdmin) { this.verifiedByAdmin = verifiedByAdmin; }
}
