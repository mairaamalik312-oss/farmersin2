package dao;

import database.DBConnection;
import model.PasswordResetToken;

import java.sql.*;

public class password_reset_tokens {

    // 1. Create a new Password Reset Token
    public boolean addToken(PasswordResetToken token) throws SQLException {
        String query = "INSERT INTO password_reset_tokens (user_id, token_hash, expires_at) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, token.getUserId());
            stmt.setString(2, token.getTokenHash());
            stmt.setTimestamp(3, token.getExpiresAt());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        token.setTokenId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get active, non-expired, and unused token by hash
    public PasswordResetToken getValidTokenByHash(String tokenHash) throws SQLException {
        String query = "SELECT * FROM password_reset_tokens WHERE token_hash = ? AND used_at IS NULL AND expires_at > CURRENT_TIMESTAMP";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tokenHash);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToToken(rs);
                }
            }
        }
        return null;
    }

    // 3. Mark token as used
    public boolean markTokenAsUsed(String tokenHash) throws SQLException {
        String query = "UPDATE password_reset_tokens SET used_at = CURRENT_TIMESTAMP WHERE token_hash = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tokenHash);
            return stmt.executeUpdate() > 0;
        }
    }

    // 4. Invalidate all previous unused tokens for a specific user
    public boolean invalidateAllUserTokens(int userId) throws SQLException {
        String query = "UPDATE password_reset_tokens SET used_at = CURRENT_TIMESTAMP WHERE user_id = ? AND used_at IS NULL";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        }
    }

    // 5. Clean up expired tokens (useful for maintenance tasks/cron jobs)
    public int deleteExpiredTokens() throws SQLException {
        String query = "DELETE FROM password_reset_tokens WHERE expires_at < CURRENT_TIMESTAMP OR used_at IS NOT NULL";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            return stmt.executeUpdate();
        }
    }

    // Helper method to map ResultSet row to PasswordResetToken Object
    private PasswordResetToken mapResultSetToToken(ResultSet rs) throws SQLException {
        PasswordResetToken token = new PasswordResetToken();
        token.setTokenId(rs.getInt("token_id"));
        token.setUserId(rs.getInt("user_id"));
        token.setTokenHash(rs.getString("token_hash"));
        token.setExpiresAt(rs.getTimestamp("expires_at"));
        token.setUsedAt(rs.getTimestamp("used_at"));
        return token;
    }
}
