package dao;

import database.DBConnection;
import model.Order;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class orders {

    // 1. Create a New Order
    public boolean addOrder(Order order) throws SQLException {
        String query = "INSERT INTO orders (buyer_id, supplier_id, delivery_address_id, product_total, " +
                "delivery_charge, discount_amount, total_amount, advance_percentage, advance_amount, " +
                "remaining_amount, order_status, payment_status, expected_delivery_date, notes) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, order.getBuyerId());
            stmt.setInt(2, order.getSupplierId());
            stmt.setInt(3, order.getDeliveryAddressId());
            stmt.setBigDecimal(4, order.getProductTotal());
            stmt.setBigDecimal(5, order.getDeliveryCharge());
            stmt.setBigDecimal(6, order.getDiscountAmount());
            stmt.setBigDecimal(7, order.getTotalAmount());
            stmt.setBigDecimal(8, order.getAdvancePercentage());
            stmt.setBigDecimal(9, order.getAdvanceAmount());
            stmt.setBigDecimal(10, order.getRemainingAmount());
            stmt.setString(11, order.getOrderStatus() != null ? order.getOrderStatus() : "PENDING");
            stmt.setString(12, order.getPaymentStatus() != null ? order.getPaymentStatus() : "UNPAID");

            if (order.getExpectedDeliveryDate() != null) {
                stmt.setDate(13, order.getExpectedDeliveryDate());
            } else {
                stmt.setNull(13, Types.DATE);
            }

            if (order.getNotes() != null) {
                stmt.setString(14, order.getNotes());
            } else {
                stmt.setNull(14, Types.VARCHAR);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        order.setOrderId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Order by ID
    public Order getOrderById(int orderId) throws SQLException {
        String query = "SELECT * FROM orders WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrder(rs);
                }
            }
        }
        return null;
    }

    // 3. Get All Orders for a Specific Buyer
    public List<Order> getOrdersByBuyerId(int buyerId) throws SQLException {
        List<Order> list = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE buyer_id = ? ORDER BY order_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        }
        return list;
    }

    // 4. Get All Orders for a Specific Supplier
    public List<Order> getOrdersBySupplierId(int supplierId) throws SQLException {
        List<Order> list = new ArrayList<>();
        String query = "SELECT * FROM orders WHERE supplier_id = ? ORDER BY order_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrder(rs));
                }
            }
        }
        return list;
    }

    // 5. Update Order Status
    public boolean updateOrderStatus(int orderId, String status) throws SQLException {
        String query = "UPDATE orders SET order_status = ? WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Update Payment Status
    public boolean updatePaymentStatus(int orderId, String paymentStatus) throws SQLException {
        String query = "UPDATE orders SET payment_status = ? WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, paymentStatus);
            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to Order Object
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getInt("order_id"));
        order.setBuyerId(rs.getInt("buyer_id"));
        order.setSupplierId(rs.getInt("supplier_id"));
        order.setDeliveryAddressId(rs.getInt("delivery_address_id"));
        order.setOrderDate(rs.getTimestamp("order_date"));
        order.setProductTotal(rs.getBigDecimal("product_total"));
        order.setDeliveryCharge(rs.getBigDecimal("delivery_charge"));
        order.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setAdvancePercentage(rs.getBigDecimal("advance_percentage"));
        order.setAdvanceAmount(rs.getBigDecimal("advance_amount"));
        order.setRemainingAmount(rs.getBigDecimal("remaining_amount"));
        order.setOrderStatus(rs.getString("order_status"));
        order.setPaymentStatus(rs.getString("payment_status"));
        order.setExpectedDeliveryDate(rs.getDate("expected_delivery_date"));
        order.setNotes(rs.getString("notes"));
        return order;
    }
}
