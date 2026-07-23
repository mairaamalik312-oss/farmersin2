package dao;

import database.DBConnection;
import model.AdminLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class admin_logs {

    // 1. Create/Insert a New Admin Action Log Record
    public boolean addLog(AdminLog log) throws SQLException {
        String query = "INSERT INTO admin_logs (admin_user_id, action, entity_type, entity_id, details) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, log.getAdminUserId());
            stmt.setString(2, log.getAction());
            stmt.setString(3, log.getEntityType());

            if (log.getEntityId() != null) {
                stmt.setInt(4, log.getEntityId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            if (log.getDetails() != null) {
                stmt.setString(5, log.getDetails());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        log.setLogId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Log Entry by Log ID
    public AdminLog getLogById(int logId) throws SQLException {
        String query = "SELECT * FROM admin_logs WHERE log_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, logId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdminLog(rs);
                }
            }
        }
        return null;
    }

    // 3. Get Logs Performed by a Specific Admin User
    public List<AdminLog> getLogsByAdminUserId(int adminUserId) throws SQLException {
        List<AdminLog> list = new ArrayList<>();
        String query = "SELECT * FROM admin_logs WHERE admin_user_id = ? ORDER BY action_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, adminUserId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAdminLog(rs));
                }
            }
        }
        return list;
    }

    // 4. Get Logs Filtered by Entity Type (e.g., 'BUYER_PROFILE', 'SUPPLIER_PRODUCT', 'USER')
    public List<AdminLog> getLogsByEntityType(String entityType) throws SQLException {
        List<AdminLog> list = new ArrayList<>();
        String query = "SELECT * FROM admin_logs WHERE entity_type = ? ORDER BY action_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, entityType);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAdminLog(rs));
                }
            }
        }
        return list;
    }

    // 5. Get Audit Trail for a Specific Entity (e.g., entity_type = 'USER', entity_id = 15)
    public List<AdminLog> getLogsByEntity(String entityType, int entityId) throws SQLException {
        List<AdminLog> list = new ArrayList<>();
        String query = "SELECT * FROM admin_logs WHERE entity_type = ? AND entity_id = ? ORDER BY action_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, entityType);
            stmt.setInt(2, entityId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAdminLog(rs));
                }
            }
        }
        return list;
    }

    // 6. Fetch All Admin Logs (For Main System Audit View)
    public List<AdminLog> getAllLogs() throws SQLException {
        List<AdminLog> list = new ArrayList<>();
        String query = "SELECT * FROM admin_logs ORDER BY action_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToAdminLog(rs));
            }
        }
        return list;
    }

    // Helper method to map ResultSet row to AdminLog Object
    private AdminLog mapResultSetToAdminLog(ResultSet rs) throws SQLException {
        AdminLog log = new AdminLog();
        log.setLogId(rs.getInt("log_id"));
        log.setAdminUserId(rs.getInt("admin_user_id"));
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entity_type"));

        int entityId = rs.getInt("entity_id");
        log.setEntityId(rs.wasNull() ? null : entityId);

        log.setDetails(rs.getString("details"));
        log.setActionDate(rs.getTimestamp("action_date"));
        return log;
    }
}
