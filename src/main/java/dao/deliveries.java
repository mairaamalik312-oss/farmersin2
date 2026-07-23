package dao;

import database.DBConnection;
import model.Delivery;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class deliveries {

    // 1. Create a New Delivery Record
    public boolean addDelivery(Delivery delivery) throws SQLException {
        String query = "INSERT INTO deliveries (order_id, delivery_method, driver_name, driver_phone, " +
                "vehicle_number, dispatch_date, delivery_date, delivery_status, delivery_proof, received_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, delivery.getOrderId());
            stmt.setString(2, delivery.getDeliveryMethod());

            setNullableString(stmt, 3, delivery.getDriverName());
            setNullableString(stmt, 4, delivery.getDriverPhone());
            setNullableString(stmt, 5, delivery.getVehicleNumber());
            setNullableTimestamp(stmt, 6, delivery.getDispatchDate());
            setNullableTimestamp(stmt, 7, delivery.getDeliveryDate());

            stmt.setString(8, delivery.getDeliveryStatus() != null ? delivery.getDeliveryStatus() : "PENDING");

            setNullableString(stmt, 9, delivery.getDeliveryProof());
            setNullableString(stmt, 10, delivery.getReceivedBy());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        delivery.setDeliveryId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Delivery Record by Delivery ID
    public Delivery getDeliveryById(int deliveryId) throws SQLException {
        String query = "SELECT * FROM deliveries WHERE delivery_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, deliveryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDelivery(rs);
                }
            }
        }
        return null;
    }

    // 3. Get Delivery Record by Order ID (1:1 Relationship)
    public Delivery getDeliveryByOrderId(int orderId) throws SQLException {
        String query = "SELECT * FROM deliveries WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDelivery(rs);
                }
            }
        }
        return null;
    }

    // 4. Update Driver / Vehicle Logistics Information
    public boolean updateLogisticsInfo(int deliveryId, String driverName, String driverPhone, String vehicleNumber) throws SQLException {
        String query = "UPDATE deliveries SET driver_name = ?, driver_phone = ?, vehicle_number = ? WHERE delivery_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            setNullableString(stmt, 1, driverName);
            setNullableString(stmt, 2, driverPhone);
            setNullableString(stmt, 3, vehicleNumber);
            stmt.setInt(4, deliveryId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 5. Mark Delivery as Dispatched
    public boolean markAsDispatched(int deliveryId) throws SQLException {
        String query = "UPDATE deliveries SET delivery_status = 'DISPATCHED', dispatch_date = CURRENT_TIMESTAMP WHERE delivery_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, deliveryId);
            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Mark Delivery as Complete (Delivered)
    public boolean markAsDelivered(int deliveryId, String deliveryProof, String receivedBy) throws SQLException {
        String query = "UPDATE deliveries SET delivery_status = 'DELIVERED', delivery_date = CURRENT_TIMESTAMP, " +
                "delivery_proof = ?, received_by = ? WHERE delivery_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            setNullableString(stmt, 1, deliveryProof);
            setNullableString(stmt, 2, receivedBy);
            stmt.setInt(3, deliveryId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 7. Generic Delivery Status Update (e.g., IN_TRANSIT, FAILED)
    public boolean updateStatus(int deliveryId, String status) throws SQLException {
        String query = "UPDATE deliveries SET delivery_status = ? WHERE delivery_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setInt(2, deliveryId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 8. Get Deliveries Filtered by Status
    public List<Delivery> getDeliveriesByStatus(String status) throws SQLException {
        List<Delivery> list = new ArrayList<>();
        String query = "SELECT * FROM deliveries WHERE delivery_status = ? ORDER BY delivery_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToDelivery(rs));
                }
            }
        }
        return list;
    }

    // Helper: Map ResultSet to Delivery Object
    private Delivery mapResultSetToDelivery(ResultSet rs) throws SQLException {
        Delivery d = new Delivery();
        d.setDeliveryId(rs.getInt("delivery_id"));
        d.setOrderId(rs.getInt("order_id"));
        d.setDeliveryMethod(rs.getString("delivery_method"));
        d.setDriverName(rs.getString("driver_name"));
        d.setDriverPhone(rs.getString("driver_phone"));
        d.setVehicleNumber(rs.getString("vehicle_number"));
        d.setDispatchDate(rs.getTimestamp("dispatch_date"));
        d.setDeliveryDate(rs.getTimestamp("delivery_date"));
        d.setDeliveryStatus(rs.getString("delivery_status"));
        d.setDeliveryProof(rs.getString("delivery_proof"));
        d.setReceivedBy(rs.getString("received_by"));
        return d;
    }

    // Null-safe setter helpers
    private void setNullableString(PreparedStatement stmt, int index, String value) throws SQLException {
        if (value != null) {
            stmt.setString(index, value);
        } else {
            stmt.setNull(index, Types.VARCHAR);
        }
    }

    private void setNullableTimestamp(PreparedStatement stmt, int index, Timestamp value) throws SQLException {
        if (value != null) {
            stmt.setTimestamp(index, value);
        } else {
            stmt.setNull(index, Types.TIMESTAMP);
        }
    }
}
