package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Order {
    private int orderId;
    private int buyerId;
    private int supplierId;
    private int deliveryAddressId;
    private Timestamp orderDate;
    private BigDecimal productTotal;
    private BigDecimal deliveryCharge;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private BigDecimal advancePercentage;
    private BigDecimal advanceAmount;
    private BigDecimal remainingAmount;
    private String orderStatus; // PENDING, ACCEPTED, PROCESSING, DISPATCHED, DELIVERED, CANCELLED, REJECTED
    private String paymentStatus; // UNPAID, ADVANCE_PAID, PARTIALLY_PAID, FULLY_PAID, REFUNDED
    private Date expectedDeliveryDate;
    private String notes;

    // Default Constructor
    public Order() {}

    // Constructor for creating a new order
    public Order(int buyerId, int supplierId, int deliveryAddressId, BigDecimal productTotal,
                 BigDecimal deliveryCharge, BigDecimal discountAmount, BigDecimal advancePercentage,
                 Date expectedDeliveryDate, String notes) {
        this.buyerId = buyerId;
        this.supplierId = supplierId;
        this.deliveryAddressId = deliveryAddressId;
        this.productTotal = productTotal != null ? productTotal : BigDecimal.ZERO;
        this.deliveryCharge = deliveryCharge != null ? deliveryCharge : BigDecimal.ZERO;
        this.discountAmount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
        this.advancePercentage = advancePercentage != null ? advancePercentage : BigDecimal.ZERO;
        this.expectedDeliveryDate = expectedDeliveryDate;
        this.notes = notes;
        this.orderStatus = "PENDING";
        this.paymentStatus = "UNPAID";

        // Auto-calculate financial totals
        calculateTotals();
    }

    // Helper method to compute total, advance, and remaining amounts
    public void calculateTotals() {
        if (this.productTotal == null) this.productTotal = BigDecimal.ZERO;
        if (this.deliveryCharge == null) this.deliveryCharge = BigDecimal.ZERO;
        if (this.discountAmount == null) this.discountAmount = BigDecimal.ZERO;
        if (this.advancePercentage == null) this.advancePercentage = BigDecimal.ZERO;

        // total_amount = product_total + delivery_charge - discount_amount
        this.totalAmount = this.productTotal.add(this.deliveryCharge).subtract(this.discountAmount);

        // advance_amount = total_amount * (advance_percentage / 100)
        this.advanceAmount = this.totalAmount
                .multiply(this.advancePercentage)
                .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);

        // remaining_amount = total_amount - advance_amount
        this.remainingAmount = this.totalAmount.subtract(this.advanceAmount);
    }

    // Getters and Setters
    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public int getDeliveryAddressId() { return deliveryAddressId; }
    public void setDeliveryAddressId(int deliveryAddressId) { this.deliveryAddressId = deliveryAddressId; }

    public Timestamp getOrderDate() { return orderDate; }
    public void setOrderDate(Timestamp orderDate) { this.orderDate = orderDate; }

    public BigDecimal getProductTotal() { return productTotal; }
    public void setProductTotal(BigDecimal productTotal) { this.productTotal = productTotal; }

    public BigDecimal getDeliveryCharge() { return deliveryCharge; }
    public void setDeliveryCharge(BigDecimal deliveryCharge) { this.deliveryCharge = deliveryCharge; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getAdvancePercentage() { return advancePercentage; }
    public void setAdvancePercentage(BigDecimal advancePercentage) { this.advancePercentage = advancePercentage; }

    public BigDecimal getAdvanceAmount() { return advanceAmount; }
    public void setAdvanceAmount(BigDecimal advanceAmount) { this.advanceAmount = advanceAmount; }

    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public void setRemainingAmount(BigDecimal remainingAmount) { this.remainingAmount = remainingAmount; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public Date getExpectedDeliveryDate() { return expectedDeliveryDate; }
    public void setExpectedDeliveryDate(Date expectedDeliveryDate) { this.expectedDeliveryDate = expectedDeliveryDate; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}