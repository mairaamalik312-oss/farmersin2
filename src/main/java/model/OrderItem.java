package model;

import java.math.BigDecimal;

public class OrderItem {
    private int orderItemId;
    private int orderId;
    private int supplierProductId;
    private BigDecimal quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private String qualityGrade;

    // Default Constructor
    public OrderItem() {}

    // Constructor for creating a new order item (automatically calculates subtotal)
    public OrderItem(int orderId, int supplierProductId, BigDecimal quantity, BigDecimal unitPrice, String qualityGrade) {
        this.orderId = orderId;
        this.supplierProductId = supplierProductId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = (quantity != null && unitPrice != null) ? quantity.multiply(unitPrice) : BigDecimal.ZERO;
        this.qualityGrade = qualityGrade;
    }

    // Full Constructor
    public OrderItem(int orderItemId, int orderId, int supplierProductId, BigDecimal quantity,
                     BigDecimal unitPrice, BigDecimal subtotal, String qualityGrade) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.supplierProductId = supplierProductId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.qualityGrade = qualityGrade;
    }

    // Getters and Setters
    public int getOrderItemId() { return orderItemId; }
    public void setOrderItemId(int orderItemId) { this.orderItemId = orderItemId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public int getSupplierProductId() { return supplierProductId; }
    public void setSupplierProductId(int supplierProductId) { this.supplierProductId = supplierProductId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public String getQualityGrade() { return qualityGrade; }
    public void setQualityGrade(String qualityGrade) { this.qualityGrade = qualityGrade; }
}
