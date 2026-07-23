package dao;

import database.DBConnection;
import model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class messages {

    // 1. Send / Create a new Message
    public boolean addMessage(Message message) throws SQLException {
        String query = "INSERT INTO messages (conversation_id, sender_user_id, message_text, is_read) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, message.getConversationId());
            stmt.setInt(2, message.getSenderUserId());
            stmt.setString(3, message.getMessageText());
            stmt.setBoolean(4, message.isRead());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        message.setMessageId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Message by ID
    public Message getMessageById(int messageId) throws SQLException {
        String query = "SELECT * FROM messages WHERE message_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, messageId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMessage(rs);
                }
            }
        }
        return null;
    }

    // 3. Get all messages for a specific conversation (Ordered chronologically)
    public List<Message> getMessagesByConversationId(int conversationId) throws SQLException {
        List<Message> messageList = new ArrayList<>();
        String query = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY sent_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, conversationId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messageList.add(mapResultSetToMessage(rs));
                }
            }
        }
        return messageList;
    }

    // 4. Mark all messages in a conversation as read for a specific recipient
    public boolean markMessagesAsRead(int conversationId, int currentUserId) throws SQLException {
        // Marks messages as read where sender is NOT the current user
        String query = "UPDATE messages SET is_read = 1 WHERE conversation_id = ? AND sender_user_id != ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, conversationId);
            stmt.setInt(2, currentUserId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 5. Get unread message count for a user in a specific conversation
    public int getUnreadMessageCount(int conversationId, int recipientUserId) throws SQLException {
        String query = "SELECT COUNT(*) FROM messages WHERE conversation_id = ? AND sender_user_id != ? AND is_read = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, conversationId);
            stmt.setInt(2, recipientUserId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // 6. Delete a Message
    public boolean deleteMessage(int messageId) throws SQLException {
        String query = "DELETE FROM messages WHERE message_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, messageId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to Message Object
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        Message message = new Message();
        message.setMessageId(rs.getInt("message_id"));
        message.setConversationId(rs.getInt("conversation_id"));
        message.setSenderUserId(rs.getInt("sender_user_id"));
        message.setMessageText(rs.getString("message_text"));
        message.setSentAt(rs.getTimestamp("sent_at"));
        message.setRead(rs.getBoolean("is_read"));
        return message;
    }
}