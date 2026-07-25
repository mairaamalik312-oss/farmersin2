package services;

import dao.cart_items;
import model.CartItem;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CartItemService {

    private final cart_items cartItemDAO;

    public CartItemService() {
        this.cartItemDAO = new cart_items();
    }

    public CartItemService(cart_items cartItemDAO) {
        if (cartItemDAO == null) {
            throw new IllegalArgumentException("Cart item DAO cannot be null.");
        }

        this.cartItemDAO = cartItemDAO;
    }

    public boolean addCartItem(CartItem item) throws SQLException {
        validateCartItem(item);

        CartItem existingItem =
                cartItemDAO.getCartItemByCartAndProduct(
                        item.getCartId(),
                        item.getSupplierProductId()
                );

        if (existingItem != null) {
            BigDecimal updatedQuantity =
                    existingItem.getQuantity().add(item.getQuantity());

            return cartItemDAO.updateQuantity(
                    existingItem.getCartItemId(),
                    updatedQuantity
            );
        }

        return cartItemDAO.addCartItem(item);
    }

    public CartItem getCartItemById(int cartItemId)
            throws SQLException {

        validateCartItemId(cartItemId);

        CartItem item =
                cartItemDAO.getCartItemById(cartItemId);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Cart item not found."
            );
        }

        return item;
    }

    public List<CartItem> getItemsByCartId(int cartId)
            throws SQLException {

        validateCartId(cartId);

        return cartItemDAO.getItemsByCartId(cartId);
    }

    public CartItem getCartItemByCartAndProduct(
            int cartId,
            int supplierProductId
    ) throws SQLException {

        validateCartId(cartId);
        validateSupplierProductId(supplierProductId);

        return cartItemDAO.getCartItemByCartAndProduct(
                cartId,
                supplierProductId
        );
    }

    public boolean updateQuantity(
            int cartItemId,
            BigDecimal quantity
    ) throws SQLException {

        validateCartItemId(cartItemId);
        validateQuantity(quantity);

        CartItem existingItem =
                cartItemDAO.getCartItemById(cartItemId);

        if (existingItem == null) {
            throw new IllegalArgumentException(
                    "Cart item not found."
            );
        }

        return cartItemDAO.updateQuantity(
                cartItemId,
                quantity
        );
    }

    public boolean deleteCartItem(int cartItemId)
            throws SQLException {

        validateCartItemId(cartItemId);

        CartItem existingItem =
                cartItemDAO.getCartItemById(cartItemId);

        if (existingItem == null) {
            throw new IllegalArgumentException(
                    "Cart item not found."
            );
        }

        return cartItemDAO.deleteCartItem(cartItemId);
    }

    public boolean clearCart(int cartId)
            throws SQLException {

        validateCartId(cartId);

        return cartItemDAO.clearCart(cartId);
    }

    private void validateCartItem(CartItem item) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "Cart item cannot be null."
            );
        }

        validateCartId(item.getCartId());
        validateSupplierProductId(
                item.getSupplierProductId()
        );
        validateQuantity(item.getQuantity());
    }

    private void validateCartItemId(int cartItemId) {
        if (cartItemId <= 0) {
            throw new IllegalArgumentException(
                    "Cart item ID must be greater than zero."
            );
        }
    }

    private void validateCartId(int cartId) {
        if (cartId <= 0) {
            throw new IllegalArgumentException(
                    "Cart ID must be greater than zero."
            );
        }
    }

    private void validateSupplierProductId(
            int supplierProductId
    ) {
        if (supplierProductId <= 0) {
            throw new IllegalArgumentException(
                    "Supplier product ID must be greater than zero."
            );
        }
    }

    private void validateQuantity(BigDecimal quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException(
                    "Quantity is required."
            );
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero."
            );
        }
    }
}