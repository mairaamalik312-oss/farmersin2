package DAO;

import database.DBConnection;
import MODEL.Address;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AddressDAO {

    // 1. Add a new address for a user
    public boolean addAddress(Address address) {
        String sql = "INSERT INTO addresses (user_id, address_type, address_line, city, area, postal_code, latitude, longitude, is_default) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, address.getUserId());
            pstmt.setString(2, address.getAddressType()); // 'BUSINESS', 'DELIVERY', 'FARM', 'BILLING'
            pstmt.setString(3, address.getAddressLine());
            pstmt.setString(4, address.getCity());
            pstmt.setString(5, address.getArea());

            if (address.getPostalCode() != null) {
                pstmt.setString(6, address.getPostalCode());
            } else {
                pstmt.setNull(6, Types.VARCHAR);
            }

            if (address.getLatitude() != null) {
                pstmt.setBigDecimal(7, address.getLatitude());
            } else {
                pstmt.setNull(7, Types.DECIMAL);
            }

            if (address.getLongitude() != null) {
                pstmt.setBigDecimal(8, address.getLongitude());
            } else {
                pstmt.setNull(8, Types.DECIMAL);
            }

            pstmt.setBoolean(9, address.isDefault());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    address.setAddressId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 2. Get all addresses for a specific user
    public List<Address> getAddressesByUserId(int userId) {
        List<Address> addresses = new ArrayList<>();
        String sql = "SELECT * FROM addresses WHERE user_id = ? ORDER BY is_default DESC, address_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                addresses.add(mapResultSetToAddress(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return addresses;
    }

    // 3. Get a single address by Address ID
    public Address getAddressById(int addressId) {
        String sql = "SELECT * FROM addresses WHERE address_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, addressId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAddress(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 4. Update an existing address
    public boolean updateAddress(Address address) {
        String sql = "UPDATE addresses SET address_type = ?, address_line = ?, city = ?, area = ?, " +
                "postal_code = ?, latitude = ?, longitude = ?, is_default = ? WHERE address_id = ?";

        try (Connection conn =DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, address.getAddressType());
            pstmt.setString(2, address.getAddressLine());
            pstmt.setString(3, address.getCity());
            pstmt.setString(4, address.getArea());
            pstmt.setString(5, address.getPostalCode());
            pstmt.setBigDecimal(6, address.getLatitude());
            pstmt.setBigDecimal(7, address.getLongitude());
            pstmt.setBoolean(8, address.isDefault());
            pstmt.setInt(9, address.getAddressId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 5. Set a default address (clears previous default first)
    public boolean setDefaultAddress(int userId, int addressId) {
        String clearDefaultSql = "UPDATE addresses SET is_default = FALSE WHERE user_id = ?";
        String setDefaultSql = "UPDATE addresses SET is_default = TRUE WHERE address_id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement clearStmt = conn.prepareStatement(clearDefaultSql);
                 PreparedStatement setStmt = conn.prepareStatement(setDefaultSql)) {

                clearStmt.setInt(1, userId);
                clearStmt.executeUpdate();

                setStmt.setInt(1, addressId);
                setStmt.setInt(2, userId);
                int updatedRows = setStmt.executeUpdate();

                conn.commit(); // Commit transaction
                return updatedRows > 0;

            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 6. Delete an address
    public boolean deleteAddress(int addressId) {
        String sql = "DELETE FROM addresses WHERE address_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, addressId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Helper mapper method
    private Address mapResultSetToAddress(ResultSet rs) throws SQLException {
        Address address = new Address();
        address.setAddressId(rs.getInt("address_id"));
        address.setUserId(rs.getInt("user_id"));
        address.setAddressType(rs.getString("address_type"));
        address.setAddressLine(rs.getString("address_line"));
        address.setCity(rs.getString("city"));
        address.setArea(rs.getString("area"));
        address.setPostalCode(rs.getString("postal_code"));
        address.setLatitude(rs.getBigDecimal("latitude"));
        address.setLongitude(rs.getBigDecimal("longitude"));
        address.setDefault(rs.getBoolean("is_default"));
        return address;
    }
}
