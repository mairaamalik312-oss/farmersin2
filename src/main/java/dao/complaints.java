package dao;

import database.DBConnection;
import model.Complaint;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class complaints {

    // 1. Submit a New Complaint
    public boolean addComplaint(Complaint complaint) throws SQLException {
        String query = "INSERT INTO complaints (order_id, submitted_by, against_user_id, " +
                "complaint_type, description, evidence_path, complaint_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            if (complaint.getOrderId() != null) {
                stmt.setInt(1, complaint.getOrderId());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }

            stmt.setInt(2, complaint.getSubmittedBy());
            stmt.setInt(3, complaint.getAgainstUserId());
            stmt.setString(4, complaint.getComplaintType());
            stmt.setString(5, complaint.getDescription());

            if (complaint.getEvidencePath() != null) {
                stmt.setString(6, complaint.getEvidencePath());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            stmt.setString(7, complaint.getComplaintStatus() != null ? complaint.getComplaintStatus() : "OPEN");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        complaint.setComplaintId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Complaint by Complaint ID
    public Complaint getComplaintById(int complaintId) throws SQLException {
        String query = "SELECT * FROM complaints WHERE complaint_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, complaintId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToComplaint(rs);
                }
            }
        }
        return null;
    }

    // 3. Get Complaints Submitted by a User
    public List<Complaint> getComplaintsSubmittedBy(int userId) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        String query = "SELECT * FROM complaints WHERE submitted_by = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToComplaint(rs));
                }
            }
        }
        return list;
    }

    // 4. Get Complaints Filed Against a User
    public List<Complaint> getComplaintsAgainstUser(int userId) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        String query = "SELECT * FROM complaints WHERE against_user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToComplaint(rs));
                }
            }
        }
        return list;
    }

    // 5. Get Complaints Associated with an Order
    public List<Complaint> getComplaintsByOrderId(int orderId) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        String query = "SELECT * FROM complaints WHERE order_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToComplaint(rs));
                }
            }
        }
        return list;
    }

    // 6. Get Complaints Filtered by Status (For Admin Queue)
    public List<Complaint> getComplaintsByStatus(String status) throws SQLException {
        List<Complaint> list = new ArrayList<>();
        String query = "SELECT * FROM complaints WHERE complaint_status = ? ORDER BY created_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToComplaint(rs));
                }
            }
        }
        return list;
    }

    // 7. Resolve or Reject Complaint (Admin Action)
    public boolean resolveComplaint(int complaintId, String newStatus, String adminResponse) throws SQLException {
        String query = "UPDATE complaints SET complaint_status = ?, admin_response = ?, " +
                "resolved_at = CURRENT_TIMESTAMP WHERE complaint_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newStatus);
            stmt.setString(2, adminResponse);
            stmt.setInt(3, complaintId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 8. Update Status (e.g., Move to UNDER_REVIEW)
    public boolean updateStatus(int complaintId, String newStatus) throws SQLException {
        String query = "UPDATE complaints SET complaint_status = ? WHERE complaint_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newStatus);
            stmt.setInt(2, complaintId);

            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to Complaint Object
    private Complaint mapResultSetToComplaint(ResultSet rs) throws SQLException {
        Complaint complaint = new Complaint();
        complaint.setComplaintId(rs.getInt("complaint_id"));

        int orderId = rs.getInt("order_id");
        complaint.setOrderId(rs.wasNull() ? null : orderId);

        complaint.setSubmittedBy(rs.getInt("submitted_by"));
        complaint.setAgainstUserId(rs.getInt("against_user_id"));
        complaint.setComplaintType(rs.getString("complaint_type"));
        complaint.setDescription(rs.getString("description"));
        complaint.setEvidencePath(rs.getString("evidence_path"));
        complaint.setComplaintStatus(rs.getString("complaint_status"));
        complaint.setAdminResponse(rs.getString("admin_response"));
        complaint.setCreatedAt(rs.getTimestamp("created_at"));
        complaint.setResolvedAt(rs.getTimestamp("resolved_at"));
        return complaint;
    }
}