package DAO;



import database.DatabaseConnection;
import model.User;
import java.sql.*;

public class UserDAO {

    // 1. Create a new User (Registration)
    public int createUser(User user) {
        String sql = "INSERT INTO users (full_name, email, password_hash, phone, role, account_status, email_verified, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, 'PENDING', FALSE, NOW(), NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, user.getPasswordHash());
            pstmt.setString(4, user.getPhone());
            pstmt.setString(5, user.getRole()); // 'ADMIN', 'BUYER', or 'SUPPLIER'

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1); // Returns generated user_id
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // 2. Fetch User by Email (For Login Verification)
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 3. Update Email Verification Status
    public boolean verifyEmail(int userId) {
        String sql = "UPDATE users SET email_verified = TRUE, updated_at = NOW() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 4. Update Account Approval Status (Admin Action: ACTIVE, BLOCKED, REJECTED)
    public boolean updateAccountStatus(int userId, String status) {
        String sql = "UPDATE users SET account_status = ?, updated_at = NOW() WHERE user_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper mapper
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setFullName(rs.getString("full_name"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setPhone(rs.getString("phone"));
        user.setRole(rs.getString("role"));
        user.setAccountStatus(rs.getString("account_status"));
        user.setEmailVerified(rs.getBoolean("email_verified"));
        return user;
    }
}
