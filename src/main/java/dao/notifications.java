package dao;

import database.DBConnection;
import model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class notifications {

    // 1. Create a new Notification
    public boolean addNotification(Notification notification) throws SQLException {
        String query = "INSERT INTO notifications (user_id, title, message, notification_type, is_read) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getTitle());
            stmt.setString(3, notification.getMessage());
            stmt.setString(4, notification.getNotificationType());
            stmt.setBoolean(5, notification.isRead());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        notification.setNotificationId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Notification by ID
    public Notification getNotificationById(int notificationId) throws SQLException {
        String query = "SELECT * FROM notifications WHERE notification_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, notificationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNotification(rs);
                }
            }
        }
        return null;
    }

    // 3. Get all notifications for a specific user (Most recent first)
    public List<Notification> getNotificationsByUserId(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        }
        return list;
    }

    // 4. Get unread notifications for a specific user
    public List<Notification> getUnreadNotificationsByUserId(int userId) throws SQLException {
        List<Notification> list = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE user_id = ? AND is_read = 0 ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToNotification(rs));
                }
            }
        }
        return list;
    }

    // 5. Mark a single notification as read
    public boolean markAsRead(int notificationId) throws SQLException {
        String query = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Mark ALL notifications as read for a specific user
    public boolean markAllAsReadForUser(int userId) throws SQLException {
        String query = "UPDATE notifications SET is_read = 1 WHERE user_id = ? AND is_read = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    // 7. Get unread notification count for a user
    public int getUnreadCount(int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM notifications WHERE user_id = ? AND is_read = 0";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    // 8. Delete a Notification
    public boolean deleteNotification(int notificationId) throws SQLException {
        String query = "DELETE FROM notifications WHERE notification_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, notificationId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to Notification Object
    private Notification mapResultSetToNotification(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setNotificationId(rs.getInt("notification_id"));
        notification.setUserId(rs.getInt("user_id"));
        notification.setTitle(rs.getString("title"));
        notification.setMessage(rs.getString("message"));
        notification.setNotificationType(rs.getString("notification_type"));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setCreatedAt(rs.getTimestamp("created_at"));
        return notification;
    }
}
