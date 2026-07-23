package DAO;

import database.DatabaseConnection;
import model.BuyerProfile;
import java.sql.*;

public class BuyerProfileDAO {

    // 1. Create Buyer Profile
    public boolean createBuyerProfile(BuyerProfile profile) {
        String sql = "INSERT INTO buyer_profiles (user_id, business_name, business_type, registration_number, tax_number, verification_status) " +
                "VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, profile.getUserId());
            pstmt.setString(2, profile.getBusinessName());
            pstmt.setString(3, profile.getBusinessType());
            pstmt.setString(4, profile.getRegistrationNumber());
            pstmt.setString(5, profile.getTaxNumber());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 2. Fetch Buyer Profile by User ID
    public BuyerProfile getProfileByUserId(int userId) {
        String sql = "SELECT * FROM buyer_profiles WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                BuyerProfile profile = new BuyerProfile();
                profile.setBuyerId(rs.getInt("buyer_id"));
                profile.setUserId(rs.getInt("user_id"));
                profile.setBusinessName(rs.getString("business_name"));
                profile.setBusinessType(rs.getString("business_type"));
                profile.setRegistrationNumber(rs.getString("registration_number"));
                profile.setTaxNumber(rs.getString("tax_number"));
                profile.setVerificationStatus(rs.getString("verification_status"));
                return profile;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. Update Profile Verification Status (Admin Action)
    public boolean updateVerificationStatus(int buyerId, String status) {
        String sql = "UPDATE buyer_profiles SET verification_status = ? WHERE buyer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status); // 'PENDING', 'VERIFIED', 'REJECTED'
            pstmt.setInt(2, buyerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
