package DAO;

import database.DatabaseConnection;
import model.SupplierProfile;
import java.sql.*;

public class SupplierProfileDAO {

    // 1. Create Supplier Profile
    public boolean createSupplierProfile(SupplierProfile profile) {
        String sql = "INSERT INTO supplier_profiles (user_id, supplier_type, farm_or_business_name, cnic_number, registration_number, verification_status, average_rating, total_completed_orders) " +
                "VALUES (?, ?, ?, ?, ?, 'PENDING', 0.00, 0)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, profile.getUserId());
            pstmt.setString(2, profile.getSupplierType());
            pstmt.setString(3, profile.getFarmOrBusinessName());
            pstmt.setString(4, profile.getCnicNumber());
            pstmt.setString(5, profile.getRegistrationNumber());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. Fetch Supplier Profile by User ID
    public SupplierProfile getProfileByUserId(int userId) {
        String sql = "SELECT * FROM supplier_profiles WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                SupplierProfile profile = new SupplierProfile();
                profile.setSupplierId(rs.getInt("supplier_id"));
                profile.setUserId(rs.getInt("user_id"));
                profile.setSupplierType(rs.getString("supplier_type"));
                profile.setFarmOrBusinessName(rs.getString("farm_or_business_name"));
                profile.setCnicNumber(rs.getString("cnic_number"));
                profile.setRegistrationNumber(rs.getString("registration_number"));
                profile.setVerificationStatus(rs.getString("verification_status"));
                profile.setAverageRating(rs.getBigDecimal("average_rating"));
                profile.setTotalCompletedOrders(rs.getInt("total_completed_orders"));
                return profile;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. Update Verification Status (Admin Approval)
    public boolean updateVerificationStatus(int supplierId, String status) {
        String sql = "UPDATE supplier_profiles SET verification_status = ? WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, supplierId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Update Rating and Order Count after a completed order
    public boolean updateStats(int supplierId, double newRating, int completedOrders) {
        String sql = "UPDATE supplier_profiles SET average_rating = ?, total_completed_orders = ? WHERE supplier_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, newRating);
            pstmt.setInt(2, completedOrders);
            pstmt.setInt(3, supplierId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}