package services;

import dao.password_reset_tokens;
import model.PasswordResetToken;

import java.sql.SQLException;
import java.sql.Timestamp;

public class PasswordResetTokenService {

    private final password_reset_tokens tokenDAO;

    public PasswordResetTokenService() {
        this.tokenDAO = new password_reset_tokens();
    }

    public PasswordResetTokenService(
            password_reset_tokens tokenDAO
    ) {
        if (tokenDAO == null) {
            throw new IllegalArgumentException(
                    "Password reset token DAO cannot be null."
            );
        }
        this.tokenDAO = tokenDAO;
    }

    public boolean addToken(PasswordResetToken token)
            throws SQLException {

        validateToken(token);

        token.setTokenHash(
                token.getTokenHash().trim()
        );

        tokenDAO.invalidateAllUserTokens(
                token.getUserId()
        );

        return tokenDAO.addToken(token);
    }

    public PasswordResetToken getValidTokenByHash(
            String tokenHash
    ) throws SQLException {

        validateTokenHash(tokenHash);

        return tokenDAO.getValidTokenByHash(
                tokenHash.trim()
        );
    }

    public boolean markTokenAsUsed(String tokenHash)
            throws SQLException {

        validateTokenHash(tokenHash);

        PasswordResetToken token =
                tokenDAO.getValidTokenByHash(
                        tokenHash.trim()
                );

        if (token == null) {
            throw new IllegalArgumentException(
                    "Token is invalid, expired, or already used."
            );
        }

        return tokenDAO.markTokenAsUsed(
                tokenHash.trim()
        );
    }

    public boolean invalidateAllUserTokens(int userId)
            throws SQLException {

        validateUserId(userId);
        return tokenDAO.invalidateAllUserTokens(userId);
    }

    public int deleteExpiredTokens()
            throws SQLException {

        return tokenDAO.deleteExpiredTokens();
    }

    private void validateToken(
            PasswordResetToken token
    ) {
        if (token == null) {
            throw new IllegalArgumentException(
                    "Password reset token cannot be null."
            );
        }

        validateUserId(token.getUserId());
        validateTokenHash(token.getTokenHash());

        if (token.getExpiresAt() == null) {
            throw new IllegalArgumentException(
                    "Token expiry date is required."
            );
        }

        Timestamp currentTime =
                new Timestamp(System.currentTimeMillis());

        if (!token.getExpiresAt().after(currentTime)) {
            throw new IllegalArgumentException(
                    "Token expiry date must be in the future."
            );
        }
    }

    private void validateTokenHash(String tokenHash) {
        if (tokenHash == null
                || tokenHash.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Token hash is required."
            );
        }
    }

    private void validateUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be greater than zero."
            );
        }
    }
}