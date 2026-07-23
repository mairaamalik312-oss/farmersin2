package dao;

import database.DBConnection;
import model.Refund;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class refunds {

    // 1. Submit a New Refund Request
    public boolean addRefund(Refund refund) throws SQLException {
        String query = "INSERT INTO refunds (payment_id, order_id, refund_amount, refund_reason, refund_status) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, refund.getPaymentId());
            stmt.setInt(2, refund.getOrderId());
            stmt.setBigDecimal(3, refund.getRefundAmount());
            stmt.setString(4, refund.getRefundReason());
            stmt.setString(5, refund.getRefundStatus() != null ? refund.getRefundStatus() : "REQUESTED");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        refund.setRefundId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Refund Request by ID
    public Refund getRefundById(int refundId) throws SQLException {
        String query = "SELECT * FROM refunds WHERE refund_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, refundId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToRefund(rs);
                }
            }
        }
        return null;
    }

    // 3. Get Refunds for a Specific Order
    public List<Refund> getRefundsByOrderId(int orderId) throws SQLException {
        List<Refund> list = new ArrayList<>();
        String query = "SELECT * FROM refunds WHERE order_id = ? ORDER BY requested_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRefund(rs));
                }
            }
        }
        return list;
    }

    // 4. Get Refunds for a Specific Payment
    public List<Refund> getRefundsByPaymentId(int paymentId) throws SQLException {
        List<Refund> list = new ArrayList<>();
        String query = "SELECT * FROM refunds WHERE payment_id = ? ORDER BY requested_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, paymentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToRefund(rs));
                }
            }
        }
        return list;
    }

    // 5. Update Refund Status (Approve/Reject/Complete)
    public boolean updateRefundStatus(int refundId, String status) throws SQLException {
        String query = "UPDATE refunds SET refund_status = ?, processed_at = CURRENT_TIMESTAMP WHERE refund_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setInt(2, refundId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Get All Pending Refund Requests (Admin View)
    public List<Refund> getPendingRefunds() throws SQLException {
        List<Refund> list = new ArrayList<>();
        String query = "SELECT * FROM refunds WHERE refund_status = 'REQUESTED' ORDER BY requested_at ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToRefund(rs));
            }
        }
        return list;
    }

    // Helper method to map ResultSet row to Refund Object
    private Refund mapResultSetToRefund(ResultSet rs) throws SQLException {
        Refund refund = new Refund();
        refund.setRefundId(rs.getInt("refund_id"));
        refund.setPaymentId(rs.getInt("payment_id"));
        refund.setOrderId(rs.getInt("order_id"));
        refund.setRefundAmount(rs.getBigDecimal("refund_amount"));
        refund.setRefundReason(rs.getString("refund_reason"));
        refund.setRefundStatus(rs.getString("refund_status"));
        refund.setRequestedAt(rs.getTimestamp("requested_at"));
        refund.setProcessedAt(rs.getTimestamp("processed_at"));
        return refund;
    }
}
