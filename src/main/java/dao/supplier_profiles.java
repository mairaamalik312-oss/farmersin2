package dao;

import database.DBConnection;
import model.SupplierProfile;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class supplier_profiles {

    // =========================================================
    // 1. ADD SUPPLIER PROFILE
    // =========================================================
    public boolean addSupplierProfile(
            SupplierProfile profile
    ) throws SQLException {

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Supplier profile cannot be null."
            );
        }

        String query =
                "INSERT INTO supplier_profiles " +
                        "(user_id, supplier_type, farm_or_business_name, " +
                        "cnic_number, registration_number, " +
                        "verification_status, average_rating, " +
                        "total_completed_orders) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn =
                     DBConnection.getConnection();

             PreparedStatement stmt =
                     conn.prepareStatement(
                             query,
                             Statement.RETURN_GENERATED_KEYS
                     )) {

            stmt.setInt(
                    1,
                    profile.getUserId()
            );

            stmt.setString(
                    2,
                    profile.getSupplierType()
            );

            stmt.setString(
                    3,
                    profile.getFarmOrBusinessName()
            );

            stmt.setString(
                    4,
                    profile.getCnicNumber()
            );

            if (profile.getRegistrationNumber() != null) {

                stmt.setString(
                        5,
                        profile.getRegistrationNumber()
                );

            } else {

                stmt.setNull(
                        5,
                        Types.VARCHAR
                );
            }

            stmt.setString(
                    6,
                    profile.getVerificationStatus() != null
                            ? profile.getVerificationStatus()
                            : "PENDING"
            );

            stmt.setBigDecimal(
                    7,
                    profile.getAverageRating() != null
                            ? profile.getAverageRating()
                            : BigDecimal.ZERO
            );

            stmt.setInt(
                    8,
                    profile.getTotalCompletedOrders()
            );

            int affectedRows =
                    stmt.executeUpdate();

            if (affectedRows > 0) {

                try (ResultSet rs =
                             stmt.getGeneratedKeys()) {

                    if (rs.next()) {

                        profile.setSupplierId(
                                rs.getInt(1)
                        );
                    }
                }

                return true;
            }
        }

        return false;
    }

    // =========================================================
    // 2. GET SUPPLIER BY SUPPLIER ID
    // =========================================================
    public SupplierProfile getSupplierById(
            int supplierId
    ) throws SQLException {

        String query =
                "SELECT * FROM supplier_profiles " +
                        "WHERE supplier_id = ?";

        try (Connection conn =
                     DBConnection.getConnection();

             PreparedStatement stmt =
                     conn.prepareStatement(query)) {

            stmt.setInt(
                    1,
                    supplierId
            );

            try (ResultSet rs =
                         stmt.executeQuery()) {

                if (rs.next()) {

                    return mapResultSetToSupplierProfile(
                            rs
                    );
                }
            }
        }

        return null;
    }

    // =========================================================
    // 3. GET SUPPLIER BY USER ID
    // =========================================================
    public SupplierProfile getSupplierByUserId(
            int userId
    ) throws SQLException {

        String query =
                "SELECT * FROM supplier_profiles " +
                        "WHERE user_id = ?";

        try (Connection conn =
                     DBConnection.getConnection();

             PreparedStatement stmt =
                     conn.prepareStatement(query)) {

            stmt.setInt(
                    1,
                    userId
            );

            try (ResultSet rs =
                         stmt.executeQuery()) {

                if (rs.next()) {

                    return mapResultSetToSupplierProfile(
                            rs
                    );
                }
            }
        }

        return null;
    }

    // =========================================================
    // 4. UPDATE SUPPLIER PROFILE
    // =========================================================
    public boolean updateSupplierProfile(
            SupplierProfile profile
    ) throws SQLException {

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Supplier profile cannot be null."
            );
        }

        String query =
                "UPDATE supplier_profiles SET " +
                        "supplier_type = ?, " +
                        "farm_or_business_name = ?, " +
                        "cnic_number = ?, " +
                        "registration_number = ? " +
                        "WHERE supplier_id = ?";

        try (Connection conn =
                     DBConnection.getConnection();

             PreparedStatement stmt =
                     conn.prepareStatement(query)) {

            stmt.setString(
                    1,
                    profile.getSupplierType()
            );

            stmt.setString(
                    2,
                    profile.getFarmOrBusinessName()
            );

            stmt.setString(
                    3,
                    profile.getCnicNumber()
            );

            if (profile.getRegistrationNumber() != null) {

                stmt.setString(
                        4,
                        profile.getRegistrationNumber()
                );

            } else {

                stmt.setNull(
                        4,
                        Types.VARCHAR
                );
            }

            stmt.setInt(
                    5,
                    profile.getSupplierId()
            );

            return stmt.executeUpdate() > 0;
        }
    }

    // =========================================================
    // 5. UPDATE VERIFICATION STATUS
    // =========================================================
    public boolean updateVerificationStatus(
            int supplierId,
            String status
    ) throws SQLException {

        String query =
                "UPDATE supplier_profiles " +
                        "SET verification_status = ? " +
                        "WHERE supplier_id = ?";

        try (Connection conn =
                     DBConnection.getConnection();

             PreparedStatement stmt =
                     conn.prepareStatement(query)) {

            stmt.setString(
                    1,
                    status
            );

            stmt.setInt(
                    2,
                    supplierId
            );

            return stmt.executeUpdate() > 0;
        }
    }

    // =========================================================
    // 6. UPDATE SUPPLIER STATS
    // =========================================================
    public boolean updateSupplierStats(
            int supplierId,
            BigDecimal averageRating,
            int incrementCompletedOrders
    ) throws SQLException {

        String query =
                "UPDATE supplier_profiles SET " +
                        "average_rating = ?, " +
                        "total_completed_orders = " +
                        "total_completed_orders + ? " +
                        "WHERE supplier_id = ?";

        try (Connection conn =
                     DBConnection.getConnection();

             PreparedStatement stmt =
                     conn.prepareStatement(query)) {

            stmt.setBigDecimal(
                    1,
                    averageRating
            );

            stmt.setInt(
                    2,
                    incrementCompletedOrders
            );

            stmt.setInt(
                    3,
                    supplierId
            );

            return stmt.executeUpdate() > 0;
        }
    }

    // =========================================================
    // 7. GET PENDING SUPPLIER VERIFICATIONS
    // =========================================================
    public List<SupplierProfile> getPendingVerifications()
            throws SQLException {

        List<SupplierProfile> list =
                new ArrayList<>();

        String query =
                "SELECT * FROM supplier_profiles " +
                        "WHERE verification_status = 'PENDING'";

        try (Connection conn =
                     DBConnection.getConnection();

             PreparedStatement stmt =
                     conn.prepareStatement(query);

             ResultSet rs =
                     stmt.executeQuery()) {

            while (rs.next()) {

                list.add(
                        mapResultSetToSupplierProfile(
                                rs
                        )
                );
            }
        }

        return list;
    }

    // =========================================================
    // HELPER METHOD
    // =========================================================
    private SupplierProfile mapResultSetToSupplierProfile(
            ResultSet rs
    ) throws SQLException {

        SupplierProfile profile =
                new SupplierProfile();

        profile.setSupplierId(
                rs.getInt(
                        "supplier_id"
                )
        );

        profile.setUserId(
                rs.getInt(
                        "user_id"
                )
        );

        profile.setSupplierType(
                rs.getString(
                        "supplier_type"
                )
        );

        profile.setFarmOrBusinessName(
                rs.getString(
                        "farm_or_business_name"
                )
        );

        profile.setCnicNumber(
                rs.getString(
                        "cnic_number"
                )
        );

        profile.setRegistrationNumber(
                rs.getString(
                        "registration_number"
                )
        );

        profile.setVerificationStatus(
                rs.getString(
                        "verification_status"
                )
        );

        profile.setAverageRating(
                rs.getBigDecimal(
                        "average_rating"
                )
        );

        profile.setTotalCompletedOrders(
                rs.getInt(
                        "total_completed_orders"
                )
        );

        return profile;
    }
}