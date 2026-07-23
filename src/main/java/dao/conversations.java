package dao;

import database.DBConnection;
import model.Conversation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class conversations {

    // 1. Create a New Conversation
    public boolean addConversation(Conversation conversation) throws SQLException {
        String query = "INSERT INTO conversations (buyer_id, supplier_id, order_id) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
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

        try (Connection conn = DBConnection.getConnection();
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

    // 3. Get Existing Conversation Between Buyer and Supplier
    public Conversation getConversationByBuyerAndSupplier(int buyerId, int supplierId) throws SQLException {
        String query = "SELECT * FROM conversations WHERE buyer_id = ? AND supplier_id = ? " +
                "ORDER BY created_at DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
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

    // 4. Get or Create Conversation (Helper for opening direct chat)
    public Conversation getOrCreateConversation(int buyerId, int supplierId, Integer orderId) throws SQLException {
        Conversation conv = getConversationByBuyerAndSupplier(buyerId, supplierId);
        if (conv == null) {
            conv = new Conversation(buyerId, supplierId, orderId);
            if (addConversation(conv)) {
                return getConversationById(conv.getConversationId());
            }
        }
        return conv;
    }

    // 5. Get All Conversations for a Specific Buyer
    public List<Conversation> getConversationsByBuyerId(int buyerId) throws SQLException {
        List<Conversation> list = new ArrayList<>();
        String query = "SELECT * FROM conversations WHERE buyer_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
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

    // 6. Get All Conversations for a Specific Supplier
    public List<Conversation> getConversationsBySupplierId(int supplierId) throws SQLException {
        List<Conversation> list = new ArrayList<>();
        String query = "SELECT * FROM conversations WHERE supplier_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
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

    // 7. Get Conversation Linked to a Specific Order ID
    public Conversation getConversationByOrderId(int orderId) throws SQLException {
        String query = "SELECT * FROM conversations WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
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

    // 8. Delete Conversation
    public boolean deleteConversation(int conversationId) throws SQLException {
        String query = "DELETE FROM conversations WHERE conversation_id = ?";

        try (Connection conn = DBConnection.getConnection();
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
        conversation.setOrderId(rs.wasNull() ? null : orderId);

        conversation.setCreatedAt(rs.getTimestamp("created_at"));
        return conversation;
    }
}