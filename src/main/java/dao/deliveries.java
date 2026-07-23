package dao;

import database.DatabaseConnection;
import model.Delivery;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class deliveries {

    // 1. Create a Delivery record
    public boolean addDelivery(Delivery delivery) throws SQLException {
        String query = "INSERT INTO deliveries (order_id, delivery_method, driver_name, driver_phone, " +
                "vehicle_number, dispatch_date, delivery_date, delivery_status, delivery_proof, received_by) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, delivery.getOrderId());
            stmt.setString(2, delivery.getDeliveryMethod());
            stmt.setString(3, delivery.getDriverName());
            stmt.setString(4, delivery.getDriverPhone());
            stmt.setString(5, delivery.getVehicleNumber());
            stmt.setTimestamp(6, delivery.getDispatchDate());
            stmt.setTimestamp(7, delivery.getDeliveryDate());
            stmt.setString(8, delivery.getDeliveryStatus() != null ? delivery.getDeliveryStatus() : "PENDING");
            stmt.setString(9, delivery.getDeliveryProof());
            stmt.setString(10, delivery.getReceivedBy());

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

    // 2. Get Delivery by Delivery ID
    public Delivery getDeliveryById(int deliveryId) throws SQLException {
        String query = "SELECT * FROM deliveries WHERE delivery_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
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

    // 3. Get Delivery by Order ID (Unique Constraint: 1 Order has 1 Delivery)
    public Delivery getDeliveryByOrderId(int orderId) throws SQLException {
        String query = "SELECT * FROM deliveries WHERE order_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
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

    // 4. Update Driver and Dispatch details (When order is dispatched)
    public boolean updateDispatchInfo(int deliveryId, String driverName, String driverPhone,
                                      String vehicleNumber, Timestamp dispatchDate) throws SQLException {
        String query = "UPDATE deliveries SET driver_name = ?, driver_phone = ?, vehicle_number = ?, " +
                "dispatch_date = ?, delivery_status = 'DISPATCHED' WHERE delivery_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, driverName);
            stmt.setString(2, driverPhone);
            stmt.setString(3, vehicleNumber);
            stmt.setTimestamp(4, dispatchDate);
            stmt.setInt(5, deliveryId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 5. Update Status and Proof upon Delivery completion/failure
    public boolean updateDeliveryStatus(int deliveryId, String status, Timestamp deliveryDate,
                                        String deliveryProof, String receivedBy) throws SQLException {
        String query = "UPDATE deliveries SET delivery_status = ?, delivery_date = ?, " +
                "delivery_proof = ?, received_by = ? WHERE delivery_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setTimestamp(2, deliveryDate);
            stmt.setString(3, deliveryProof);
            stmt.setString(4, receivedBy);
            stmt.setInt(5, deliveryId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Get Deliveries by Status (Useful for logistics tracking)
    public List<Delivery> getDeliveriesByStatus(String status) throws SQLException {
        List<Delivery> list = new ArrayList<>();
        String query = "SELECT * FROM deliveries WHERE delivery_status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
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

    // 7. Delete Delivery
    public boolean deleteDelivery(int deliveryId) throws SQLException {
        String query = "DELETE FROM deliveries WHERE delivery_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, deliveryId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to Delivery Object
    private Delivery mapResultSetToDelivery(ResultSet rs) throws SQLException {
        Delivery delivery = new Delivery();
        delivery.setDeliveryId(rs.getInt("delivery_id"));
        delivery.setOrderId(rs.getInt("order_id"));
        delivery.setDeliveryMethod(rs.getString("delivery_method"));
        delivery.setDriverName(rs.getString("driver_name"));
        delivery.setDriverPhone(rs.getString("driver_phone"));
        delivery.setVehicleNumber(rs.getString("vehicle_number"));
        delivery.setDispatchDate(rs.getTimestamp("dispatch_date"));
        delivery.setDeliveryDate(rs.getTimestamp("delivery_date"));
        delivery.setDeliveryStatus(rs.getString("delivery_status"));
        delivery.setDeliveryProof(rs.getString("delivery_proof"));
        delivery.setReceivedBy(rs.getString("received_by"));
        return delivery;
    }
}
