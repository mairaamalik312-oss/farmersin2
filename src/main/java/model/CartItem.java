package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class CartItem {
    private int cartItemId;
    private int cartId;
    private int supplierProductId;
    private BigDecimal quantity;
    private Timestamp addedAt;

    // Default Constructor
    public CartItem() {}

    // Constructor for adding a new item to the cart
    public CartItem(int cartId, int supplierProductId, BigDecimal quantity) {
        this.cartId = cartId;
        this.supplierProductId = supplierProductId;
        this.quantity = quantity;
    }

    // Full Constructor
    public CartItem(int cartItemId, int cartId, int supplierProductId,
                    BigDecimal quantity, Timestamp addedAt) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.supplierProductId = supplierProductId;
        this.quantity = quantity;
        this.addedAt = addedAt;
    }

    // Getters and Setters
    public int getCartItemId() { return cartItemId; }
    public void setCartItemId(int cartItemId) { this.cartItemId = cartItemId; }

    public int getCartId() { return cartId; }
    public void setCartId(int cartId) { this.cartId = cartId; }

    public int getSupplierProductId() { return supplierProductId; }
    public void setSupplierProductId(int supplierProductId) { this.supplierProductId = supplierProductId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public Timestamp getAddedAt() { return addedAt; }
    public void setAddedAt(Timestamp addedAt) { this.addedAt = addedAt; }
}
