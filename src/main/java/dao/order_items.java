package dao;

import database.DBConnection;
import model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class order_items {

    // 1. Insert a Single Order Item
    public boolean addOrderItem(OrderItem item) throws SQLException {
        String query = "INSERT INTO order_items (order_id, supplier_product_id, quantity, unit_price, subtotal, quality_grade) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, item.getOrderId());
            stmt.setInt(2, item.getSupplierProductId());
            stmt.setBigDecimal(3, item.getQuantity());
            stmt.setBigDecimal(4, item.getUnitPrice());
            stmt.setBigDecimal(5, item.getSubtotal());

            if (item.getQualityGrade() != null) {
                stmt.setString(6, item.getQualityGrade());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setOrderItemId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Insert Multiple Order Items in Batch (Optimized for Order Placement)
    public boolean addOrderItemsBatch(List<OrderItem> items) throws SQLException {
        String query = "INSERT INTO order_items (order_id, supplier_product_id, quantity, unit_price, subtotal, quality_grade) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (OrderItem item : items) {
                stmt.setInt(1, item.getOrderId());
                stmt.setInt(2, item.getSupplierProductId());
                stmt.setBigDecimal(3, item.getQuantity());
                stmt.setBigDecimal(4, item.getUnitPrice());
                stmt.setBigDecimal(5, item.getSubtotal());

                if (item.getQualityGrade() != null) {
                    stmt.setString(6, item.getQualityGrade());
                } else {
                    stmt.setNull(6, Types.VARCHAR);
                }

                stmt.addBatch();
            }

            int[] results = stmt.executeBatch();
            return results.length == items.size();
        }
    }

    // 3. Get Order Item by ID
    public OrderItem getOrderItemById(int orderItemId) throws SQLException {
        String query = "SELECT * FROM order_items WHERE order_item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderItemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToOrderItem(rs);
                }
            }
        }
        return null;
    }

    // 4. Get All Line Items for a Specific Order
    public List<OrderItem> getItemsByOrderId(int orderId) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String query = "SELECT * FROM order_items WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToOrderItem(rs));
                }
            }
        }
        return list;
    }

    // 5. Update Order Item Quantity/Subtotal
    public boolean updateOrderItem(OrderItem item) throws SQLException {
        String query = "UPDATE order_items SET quantity = ?, unit_price = ?, subtotal = ?, quality_grade = ? " +
                "WHERE order_item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBigDecimal(1, item.getQuantity());
            stmt.setBigDecimal(2, item.getUnitPrice());
            stmt.setBigDecimal(3, item.getSubtotal());

            if (item.getQualityGrade() != null) {
                stmt.setString(4, item.getQualityGrade());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.setInt(5, item.getOrderItemId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Delete Order Item by ID
    public boolean deleteOrderItem(int orderItemId) throws SQLException {
        String query = "DELETE FROM order_items WHERE order_item_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderItemId);
            return stmt.executeUpdate() > 0;
        }
    }

    // 7. Delete All Items for an Order
    public boolean deleteItemsByOrderId(int orderId) throws SQLException {
        String query = "DELETE FROM order_items WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to OrderItem Object
    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setOrderItemId(rs.getInt("order_item_id"));
        item.setOrderId(rs.getInt("order_id"));
        item.setSupplierProductId(rs.getInt("supplier_product_id"));
        item.setQuantity(rs.getBigDecimal("quantity"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setSubtotal(rs.getBigDecimal("subtotal"));
        item.setQualityGrade(rs.getString("quality_grade"));
        return item;
    }
}
