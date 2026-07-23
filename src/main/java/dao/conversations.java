package dao;

import database.DatabaseConnection; // Adjust this import based on your database connection package
import model.Conversation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class conversations {

    // 1. Create a new Conversation
    public boolean createConversation(Conversation conversation) throws SQLException {
        String query = "INSERT INTO conversations (buyer_id, supplier_id, order_id) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, conversation.getBuyerId());
            stmt.setInt(2, conversation.getSupplierId());

            if (conversation.getOrderId() != null) {
                stmt.setInt(3, conversation.getOrderId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        conversation.setConversationId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Conversation by ID
    public Conversation getConversationById(int conversationId) throws SQLException {
        String query = "SELECT * FROM conversations WHERE conversation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, conversationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToConversation(rs);
                }
            }
        }
        return null;
    }

    // 3. Find existing conversation between Buyer and Supplier (without specific order)
    public Conversation getConversationByBuyerAndSupplier(int buyerId, int supplierId) throws SQLException {
        String query = "SELECT * FROM conversations WHERE buyer_id = ? AND supplier_id = ? AND order_id IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, buyerId);
            stmt.setInt(2, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToConversation(rs);
                }
            }
        }
        return null;
    }

    // 4. Get Conversation linked to a specific Order
    public Conversation getConversationByOrderId(int orderId) throws SQLException {
        String query = "SELECT * FROM conversations WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToConversation(rs);
                }
            }
        }
        return null;
    }

    // 5. Get all Conversations for a specific Buyer
    public List<Conversation> getConversationsByBuyerId(int buyerId) throws SQLException {
        List<Conversation> list = new ArrayList<>();
        String query = "SELECT * FROM conversations WHERE buyer_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToConversation(rs));
                }
            }
        }
        return list;
    }

    // 6. Get all Conversations for a specific Supplier
    public List<Conversation> getConversationsBySupplierId(int supplierId) throws SQLException {
        List<Conversation> list = new ArrayList<>();
        String query = "SELECT * FROM conversations WHERE supplier_id = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToConversation(rs));
                }
            }
        }
        return list;
    }

    // 7. Delete Conversation
    public boolean deleteConversation(int conversationId) throws SQLException {
        String query = "DELETE FROM conversations WHERE conversation_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, conversationId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to Conversation Object
    private Conversation mapResultSetToConversation(ResultSet rs) throws SQLException {
        Conversation conversation = new Conversation();
        conversation.setConversationId(rs.getInt("conversation_id"));
        conversation.setBuyerId(rs.getInt("buyer_id"));
        conversation.setSupplierId(rs.getInt("supplier_id"));

        int orderId = rs.getInt("order_id");
        if (!rs.wasNull()) {
            conversation.setOrderId(orderId);
        } else {
            conversation.setOrderId(null);
        }

        conversation.setCreatedAt(rs.getTimestamp("created_at"));
        return conversation;
    }
}
