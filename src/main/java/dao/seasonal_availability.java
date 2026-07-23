package dao;

import database.DBConnection;
import model.SeasonalAvailability;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class seasonal_availability {

    // 1. Add a New Seasonal Availability Record
    public boolean addSeasonalAvailability(SeasonalAvailability availability) throws SQLException {
        String query = "INSERT INTO seasonal_availability (product_id, start_month, end_month, " +
                "region, is_currently_available, updated_by) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, availability.getProductId());
            stmt.setInt(2, availability.getStartMonth());
            stmt.setInt(3, availability.getEndMonth());

            if (availability.getRegion() != null) {
                stmt.setString(4, availability.getRegion());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.setBoolean(5, availability.isCurrentlyAvailable());

            if (availability.getUpdatedBy() != null) {
                stmt.setInt(6, availability.getUpdatedBy());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        availability.setAvailabilityId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Record by ID
    public SeasonalAvailability getAvailabilityById(int availabilityId) throws SQLException {
        String query = "SELECT * FROM seasonal_availability WHERE availability_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, availabilityId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSeasonalAvailability(rs);
                }
            }
        }
        return null;
    }

    // 3. Get All Availability Records for a Product
    public List<SeasonalAvailability> getAvailabilityByProductId(int productId) throws SQLException {
        List<SeasonalAvailability> list = new ArrayList<>();
        String query = "SELECT * FROM seasonal_availability WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSeasonalAvailability(rs));
                }
            }
        }
        return list;
    }

    // 4. Get Currently Available Products by Month (1-12)
    public List<SeasonalAvailability> getAvailableProductsByMonth(int month) throws SQLException {
        List<SeasonalAvailability> list = new ArrayList<>();
        // Handles standard ranges (e.g., May to Sep) and wrapping ranges (e.g., Nov to Feb)
        String query = "SELECT * FROM seasonal_availability WHERE is_currently_available = 1 AND " +
                "((start_month <= end_month AND ? BETWEEN start_month AND end_month) OR " +
                "(start_month > end_month AND (? >= start_month OR ? <= end_month)))";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, month);
            stmt.setInt(2, month);
            stmt.setInt(3, month);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSeasonalAvailability(rs));
                }
            }
        }
        return list;
    }

    // 5. Update Seasonal Availability Record
    public boolean updateSeasonalAvailability(SeasonalAvailability availability) throws SQLException {
        String query = "UPDATE seasonal_availability SET start_month = ?, end_month = ?, " +
                "region = ?, is_currently_available = ?, updated_by = ? WHERE availability_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, availability.getStartMonth());
            stmt.setInt(2, availability.getEndMonth());

            if (availability.getRegion() != null) {
                stmt.setString(3, availability.getRegion());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            stmt.setBoolean(4, availability.isCurrentlyAvailable());

            if (availability.getUpdatedBy() != null) {
                stmt.setInt(5, availability.getUpdatedBy());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            stmt.setInt(6, availability.getAvailabilityId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Delete Record
    public boolean deleteSeasonalAvailability(int availabilityId) throws SQLException {
        String query = "DELETE FROM seasonal_availability WHERE availability_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, availabilityId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to SeasonalAvailability Object
    private SeasonalAvailability mapResultSetToSeasonalAvailability(ResultSet rs) throws SQLException {
        SeasonalAvailability availability = new SeasonalAvailability();
        availability.setAvailabilityId(rs.getInt("availability_id"));
        availability.setProductId(rs.getInt("product_id"));
        availability.setStartMonth(rs.getInt("start_month"));
        availability.setEndMonth(rs.getInt("end_month"));
        availability.setRegion(rs.getString("region"));
        availability.setCurrentlyAvailable(rs.getBoolean("is_currently_available"));

        int updatedBy = rs.getInt("updated_by");
        availability.setUpdatedBy(rs.wasNull() ? null : updatedBy);

        return availability;
    }
}
