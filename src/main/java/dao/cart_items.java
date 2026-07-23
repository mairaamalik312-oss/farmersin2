package dao;

import database.DBConnection;
import model.CartItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class cart_items {

    // 1. Add an Item to the Cart
    public boolean addCartItem(CartItem item) throws SQLException {
        String query = "INSERT INTO cart_items (cart_id, supplier_product_id, quantity) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, item.getCartId());
            stmt.setInt(2, item.getSupplierProductId());
            stmt.setBigDecimal(3, item.getQuantity());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setCartItemId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Cart Item by ID
    public CartItem getCartItemById(int cartItemId) throws SQLException {
        String query = "SELECT * FROM cart_items WHERE cart_item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, cartItemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCartItem(rs);
                }
            }
        }
        return null;
    }

    // 3. Get All Items in a Specific Cart
    public List<CartItem> getItemsByCartId(int cartId) throws SQLException {
        List<CartItem> list = new ArrayList<>();
        String query = "SELECT * FROM cart_items WHERE cart_id = ? ORDER BY added_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToCartItem(rs));
                }
            }
        }
        return list;
    }

    // 4. Check if a Specific Supplier Product is Already in the Cart
    public CartItem getCartItemByCartAndProduct(int cartId, int supplierProductId) throws SQLException {
        String query = "SELECT * FROM cart_items WHERE cart_id = ? AND supplier_product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, cartId);
            stmt.setInt(2, supplierProductId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCartItem(rs);
                }
            }
        }
        return null;
    }

    // 5. Update Item Quantity
    public boolean updateQuantity(int cartItemId, java.math.BigDecimal quantity) throws SQLException {
        String query = "UPDATE cart_items SET quantity = ? WHERE cart_item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBigDecimal(1, quantity);
            stmt.setInt(2, cartItemId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Delete a Specific Item from Cart
    public boolean deleteCartItem(int cartItemId) throws SQLException {
        String query = "DELETE FROM cart_items WHERE cart_item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, cartItemId);
            return stmt.executeUpdate() > 0;
        }
    }

    // 7. Clear All Items in a Cart (Used after order placement)
    public boolean clearCart(int cartId) throws SQLException {
        String query = "DELETE FROM cart_items WHERE cart_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, cartId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to CartItem Object
    private CartItem mapResultSetToCartItem(ResultSet rs) throws SQLException {
        CartItem item = new CartItem();
        item.setCartItemId(rs.getInt("cart_item_id"));
        item.setCartId(rs.getInt("cart_id"));
        item.setSupplierProductId(rs.getInt("supplier_product_id"));
        item.setQuantity(rs.getBigDecimal("quantity"));
        item.setAddedAt(rs.getTimestamp("added_at"));
        return item;
    }
}