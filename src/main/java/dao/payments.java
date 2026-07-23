package dao;

import database.DBConnection;
import model.Payment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class payments {

    // 1. Record a New Payment
    public boolean addPayment(Payment payment) throws SQLException {
        String query = "INSERT INTO payments (order_id, buyer_id, payment_type, payment_method, " +
                "amount, transaction_reference, payment_status, proof_image_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, payment.getOrderId());
            stmt.setInt(2, payment.getBuyerId());
            stmt.setString(3, payment.getPaymentType());
            stmt.setString(4, payment.getPaymentMethod());
            stmt.setBigDecimal(5, payment.getAmount());

            if (payment.getTransactionReference() != null) {
                stmt.setString(6, payment.getTransactionReference());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            stmt.setString(7, payment.getPaymentStatus() != null ? payment.getPaymentStatus() : "PENDING");

            if (payment.getProofImagePath() != null) {
                stmt.setString(8, payment.getProofImagePath());
            } else {
                stmt.setNull(8, Types.VARCHAR);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        payment.setPaymentId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Payment by ID
    public Payment getPaymentById(int paymentId) throws SQLException {
        String query = "SELECT * FROM payments WHERE payment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, paymentId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayment(rs);
                }
            }
        }
        return null;
    }

    // 3. Get All Payments for an Order
    public List<Payment> getPaymentsByOrderId(int orderId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String query = "SELECT * FROM payments WHERE order_id = ? ORDER BY payment_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPayment(rs));
                }
            }
        }
        return list;
    }

    // 4. Get All Payments by Buyer ID
    public List<Payment> getPaymentsByBuyerId(int buyerId) throws SQLException {
        List<Payment> list = new ArrayList<>();
        String query = "SELECT * FROM payments WHERE buyer_id = ? ORDER BY payment_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToPayment(rs));
                }
            }
        }
        return list;
    }

    // 5. Verify/Approve Payment (Admin Action)
    public boolean verifyPayment(int paymentId, int adminUserId, String status) throws SQLException {
        String query = "UPDATE payments SET payment_status = ?, verified_by_admin = ?, " +
                "payment_date = CURRENT_TIMESTAMP WHERE payment_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setInt(2, adminUserId);
            stmt.setInt(3, paymentId);

            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to Payment Object
    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setOrderId(rs.getInt("order_id"));
        payment.setBuyerId(rs.getInt("buyer_id"));
        payment.setPaymentType(rs.getString("payment_type"));
        payment.setPaymentMethod(rs.getString("payment_method"));
        payment.setAmount(rs.getBigDecimal("amount"));
        payment.setTransactionReference(rs.getString("transaction_reference"));
        payment.setPaymentStatus(rs.getString("payment_status"));
        payment.setPaymentDate(rs.getTimestamp("payment_date"));
        payment.setProofImagePath(rs.getString("proof_image_path"));

        int adminId = rs.getInt("verified_by_admin");
        payment.setVerifiedByAdmin(rs.wasNull() ? null : adminId);

        return payment;
    }
}
