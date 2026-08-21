package services;

import dao.buyer_profiles;
import database.DBConnection;
import model.BuyerProfile;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Handles buyer registration as a single atomic transaction:
 * the user account and the buyer profile are created together,
 * or neither is created at all.
 *
 * This prevents the "Buyer profile not resolved" error caused by
 * users existing without a matching buyer_profiles row.
 *
 * NOTE: This class assumes you have a user DAO/service with a method
 * that can create a user using an EXISTING Connection (so it can join
 * this transaction), e.g. something like:
 *
 *     int createUser(Connection conn, String name, String email,
 *                     String passwordHash, String role) throws SQLException;
 *
 * Replace the TODO section below with your real user-creation call.
 * If your user DAO currently only opens its own connection (like the
 * original buyer_profiles DAO did), give it the same kind of
 * Connection-accepting overload shown in dao/buyer_profiles.java.
 */
public class Registrationservice {

    private final buyer_profiles buyerProfileDAO;
    // private final dao.users userDAO; // TODO: wire in your actual user DAO

    public Registrationservice() {
        this.buyerProfileDAO = new buyer_profiles();
        // this.userDAO = new dao.users();
    }

    public Registrationservice(buyer_profiles buyerProfileDAO /*, dao.users userDAO */) {
        this.buyerProfileDAO = buyerProfileDAO;
        // this.userDAO = userDAO;
    }

    /**
     * Registers a new buyer: creates the user account and the buyer
     * profile in one transaction. If either step fails, both are
     * rolled back — no orphaned user, no orphaned profile.
     *
     * @return the new user's id
     */
    public int registerBuyer(String name, String email, String passwordHash,
                             String businessName, String businessType,
                             String registrationNumber, String taxNumber) throws SQLException {

        if (isBlank(businessName) || isBlank(businessType)) {
            throw new IllegalArgumentException("Business name and business type are required.");
        }

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // begin transaction

            // ---- Step 1: create the user row ----
            // TODO: replace with your real call, e.g.:
            // int newUserId = userDAO.createUser(conn, name, email, passwordHash, "BUYER");
            int newUserId = createUserPlaceholder(conn, name, email, passwordHash);

            // ---- Step 2: create the buyer profile in the SAME transaction ----
            BuyerProfile profile = new BuyerProfile(
                    newUserId, businessName.trim(), businessType.trim().toUpperCase(),
                    cleanOptional(registrationNumber), cleanOptional(taxNumber));

            boolean created = buyerProfileDAO.addBuyerProfile(conn, profile);
            if (!created) {
                throw new SQLException("Failed to create buyer profile for user id " + newUserId + ".");
            }

            conn.commit(); // both rows now exist together
            return newUserId;

        } catch (SQLException | RuntimeException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // undo the user row too — no orphan left behind
                } catch (SQLException rollbackEx) {
                    rollbackEx.addSuppressed(e);
                    throw rollbackEx;
                }
            }
            if (e instanceof SQLException) {
                throw (SQLException) e;
            }
            throw (RuntimeException) e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {
                    // nothing further we can do here
                }
            }
        }
    }

    // Placeholder so this file compiles standalone. DELETE this method
    // once your real user DAO call is wired in above.
    private int createUserPlaceholder(Connection conn, String name, String email, String passwordHash)
            throws SQLException {
        throw new UnsupportedOperationException(
                "Wire in your real user-creation DAO call here (see TODO comments above).");
    }

    private String cleanOptional(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}