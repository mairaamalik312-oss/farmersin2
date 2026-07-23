package dao;

import database.DBConnection;
import model.Cart;

import java.sql.*;

public class carts {

    // 1. Create a New Cart for a Buyer
    public boolean addCart(Cart cart) throws SQLException {
        String query = "INSERT INTO carts (buyer_id) VALUES (?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, cart.getBuyerId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        cart.setCartId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Cart by Cart ID
    public Cart getCartById(int cartId) throws SQLException {
        String query = "SELECT * FROM carts WHERE cart_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, cartId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCart(rs);
                }
            }
        }
        return null;
    }

    // 3. Get Cart by Buyer ID (1:1 Unique Relationship)
    public Cart getCartByBuyerId(int buyerId) throws SQLException {
        String query = "SELECT * FROM carts WHERE buyer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCart(rs);
                }
            }
        }
        return null;
    }

    // 4. Get or Create Cart by Buyer ID (Convenience method for user session handling)
    public Cart getOrCreateCartByBuyerId(int buyerId) throws SQLException {
        Cart cart = getCartByBuyerId(buyerId);
        if (cart == null) {
            cart = new Cart(buyerId);
            if (addCart(cart)) {
                return getCartByBuyerId(buyerId);
            }
        }
        return cart;
    }

    // 5. Delete Cart by Cart ID
    public boolean deleteCart(int cartId) throws SQLException {
        String query = "DELETE FROM carts WHERE cart_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, cartId);
            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Delete Cart by Buyer ID
    public boolean deleteCartByBuyerId(int buyerId) throws SQLException {
        String query = "DELETE FROM carts WHERE buyer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, buyerId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to Cart Object
    private Cart mapResultSetToCart(ResultSet rs) throws SQLException {
        Cart cart = new Cart();
        cart.setCartId(rs.getInt("cart_id"));
        cart.setBuyerId(rs.getInt("buyer_id"));
        cart.setCreatedAt(rs.getTimestamp("created_at"));
        cart.setUpdatedAt(rs.getTimestamp("updated_at"));
        return cart;
    }
}