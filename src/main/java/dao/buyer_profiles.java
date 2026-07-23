package dao;

import database.DBConnection;
import model.BuyerProfile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class buyer_profiles {

    // 1. Create a New Buyer Profile
    public boolean addBuyerProfile(BuyerProfile profile) throws SQLException {
        String query = "INSERT INTO buyer_profiles (user_id, business_name, business_type, " +
                "registration_number, tax_number, verification_status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, profile.getUserId());
            stmt.setString(2, profile.getBusinessName());
            stmt.setString(3, profile.getBusinessType());

            if (profile.getRegistrationNumber() != null) {
                stmt.setString(4, profile.getRegistrationNumber());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            if (profile.getTaxNumber() != null) {
                stmt.setString(5, profile.getTaxNumber());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            stmt.setString(6, profile.getVerificationStatus() != null ? profile.getVerificationStatus() : "PENDING");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        profile.setBuyerId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Buyer Profile by Buyer ID
    public BuyerProfile getBuyerById(int buyerId) throws SQLException {
        String query = "SELECT * FROM buyer_profiles WHERE buyer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBuyerProfile(rs);
                }
            }
        }
        return null;
    }

    // 3. Get Buyer Profile by User ID (1:1 Relationship)
    public BuyerProfile getBuyerByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM buyer_profiles WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBuyerProfile(rs);
                }
            }
        }
        return null;
    }

    // 4. Update Profile Info
    public boolean updateBuyerProfile(BuyerProfile profile) throws SQLException {
        String query = "UPDATE buyer_profiles SET business_name = ?, business_type = ?, " +
                "registration_number = ?, tax_number = ? WHERE buyer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, profile.getBusinessName());
            stmt.setString(2, profile.getBusinessType());

            if (profile.getRegistrationNumber() != null) {
                stmt.setString(3, profile.getRegistrationNumber());
            } else {
                stmt.setNull(3, Types.VARCHAR);
            }

            if (profile.getTaxNumber() != null) {
                stmt.setString(4, profile.getTaxNumber());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.setInt(5, profile.getBuyerId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 5. Update Verification Status (Admin Workflow)
    public boolean updateVerificationStatus(int buyerId, String status) throws SQLException {
        String query = "UPDATE buyer_profiles SET verification_status = ? WHERE buyer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setInt(2, buyerId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Get Pending Verification Requests (For Admin Queue)
    public List<BuyerProfile> getPendingVerifications() throws SQLException {
        List<BuyerProfile> list = new ArrayList<>();
        String query = "SELECT * FROM buyer_profiles WHERE verification_status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToBuyerProfile(rs));
            }
        }
        return list;
    }

    // Helper method to map ResultSet row to BuyerProfile Object
    private BuyerProfile mapResultSetToBuyerProfile(ResultSet rs) throws SQLException {
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
}
