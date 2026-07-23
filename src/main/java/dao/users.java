package dao;

import database.DBConnection;
import model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class users {

    // 1. Register a New User
    public boolean addUser(User user) throws SQLException {
        String query = "INSERT INTO users (full_name, email, password_hash, phone, role, " +
                "account_status, email_verified) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPasswordHash());

            if (user.getPhone() != null) {
                stmt.setString(4, user.getPhone());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            stmt.setString(5, user.getRole());
            stmt.setString(6, user.getAccountStatus() != null ? user.getAccountStatus() : "PENDING");
            stmt.setBoolean(7, user.isEmailVerified());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        user.setUserId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get User by ID
    public User getUserById(int userId) throws SQLException {
        String query = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    // 3. Get User by Email (Crucial for Authentication)
    public User getUserByEmail(String email) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    // 4. Update Profile Info (Name & Phone)
    public boolean updateUserProfile(User user) throws SQLException {
        String query = "UPDATE users SET full_name = ?, phone = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getFullName());

            if (user.getPhone() != null) {
                stmt.setString(2, user.getPhone());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            stmt.setInt(3, user.getUserId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 5. Update Password
    public boolean updatePassword(int userId, String newPasswordHash) throws SQLException {
        String query = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, newPasswordHash);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Update Account Status (Admin Approval/Block Workflow)
    public boolean updateAccountStatus(int userId, String status) throws SQLException {
        String query = "UPDATE users SET account_status = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 7. Verify Email Status
    public boolean setEmailVerified(int userId, boolean isVerified) throws SQLException {
        String query = "UPDATE users SET email_verified = ? WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, isVerified);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 8. Get Users by Role (Filtering for Admin Dashboards)
    public List<User> getUsersByRole(String role) throws SQLException {
        List<User> list = new ArrayList<>();
        String query = "SELECT * FROM users WHERE role = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, role);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToUser(rs));
                }
            }
        }
        return list;
    }

    // Helper method to map ResultSet row to User Object
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
        user.setCreatedAt(rs.getTimestamp("created_at"));
        user.setUpdatedAt(rs.getTimestamp("updated_at"));
        return user;
    }
}