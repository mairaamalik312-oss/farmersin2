package model;

import java.sql.Timestamp;

public class PasswordResetToken {
    private int tokenId;
    private int userId;
    private String tokenHash;
    private Timestamp expiresAt;
    private Timestamp usedAt; // Can be NULL

    // Default Constructor
    public PasswordResetToken() {}

    // Constructor for creating a new token
    public PasswordResetToken(int userId, String tokenHash, Timestamp expiresAt) {
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
    }

    // Full Constructor
    public PasswordResetToken(int tokenId, int userId, String tokenHash,
                              Timestamp expiresAt, Timestamp usedAt) {
        this.tokenId = tokenId;
        this.userId = userId;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.usedAt = usedAt;
    }

    // Getters and Setters
    public int getTokenId() { return tokenId; }
    public void setTokenId(int tokenId) { this.tokenId = tokenId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public Timestamp getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Timestamp expiresAt) { this.expiresAt = expiresAt; }

    public Timestamp getUsedAt() { return usedAt; }
    public void setUsedAt(Timestamp usedAt) { this.usedAt = usedAt; }
}
