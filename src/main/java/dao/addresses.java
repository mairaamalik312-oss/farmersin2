package dao;

import database.DBConnection;
import model.Address;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class addresses {

    // 1. Add a New Address
    public boolean addAddress(Address addr) throws SQLException {
        // If this address is set as default, unset existing default addresses for this user first
        if (addr.isDefault()) {
            resetDefaultAddressForUser(addr.getUserId());
        }

        String query = "INSERT INTO addresses (user_id, address_type, address_line, city, area, " +
                "postal_code, latitude, longitude, is_default) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, addr.getUserId());
            stmt.setString(2, addr.getAddressType());
            stmt.setString(3, addr.getAddressLine());
            stmt.setString(4, addr.getCity());
            stmt.setString(5, addr.getArea());

            if (addr.getPostalCode() != null) {
                stmt.setString(6, addr.getPostalCode());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            if (addr.getLatitude() != null) {
                stmt.setBigDecimal(7, addr.getLatitude());
            } else {
                stmt.setNull(7, Types.DECIMAL);
            }

            if (addr.getLongitude() != null) {
                stmt.setBigDecimal(8, addr.getLongitude());
            } else {
                stmt.setNull(8, Types.DECIMAL);
            }

            stmt.setBoolean(9, addr.isDefault());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        addr.setAddressId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Address by Address ID
    public Address getAddressById(int addressId) throws SQLException {
        String query = "SELECT * FROM addresses WHERE address_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, addressId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAddress(rs);
                }
            }
        }
        return null;
    }

    // 3. Get All Addresses for a Specific User
    public List<Address> getAddressesByUserId(int userId) throws SQLException {
        List<Address> list = new ArrayList<>();
        String query = "SELECT * FROM addresses WHERE user_id = ? ORDER BY is_default DESC, address_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToAddress(rs));
                }
            }
        }
        return list;
    }

    // 4. Get Default Address for a Specific User
    public Address getDefaultAddressByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM addresses WHERE user_id = ? AND is_default = 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAddress(rs);
                }
            }
        }
        return null;
    }

    // 5. Update Existing Address
    public boolean updateAddress(Address addr) throws SQLException {
        if (addr.isDefault()) {
            resetDefaultAddressForUser(addr.getUserId());
        }

        String query = "UPDATE addresses SET address_type = ?, address_line = ?, city = ?, area = ?, " +
                "postal_code = ?, latitude = ?, longitude = ?, is_default = ? WHERE address_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, addr.getAddressType());
            stmt.setString(2, addr.getAddressLine());
            stmt.setString(3, addr.getCity());
            stmt.setString(4, addr.getArea());

            if (addr.getPostalCode() != null) {
                stmt.setString(5, addr.getPostalCode());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            if (addr.getLatitude() != null) {
                stmt.setBigDecimal(6, addr.getLatitude());
            } else {
                stmt.setNull(6, Types.DECIMAL);
            }

            if (addr.getLongitude() != null) {
                stmt.setBigDecimal(7, addr.getLongitude());
            } else {
                stmt.setNull(7, Types.DECIMAL);
            }

            stmt.setBoolean(8, addr.isDefault());
            stmt.setInt(9, addr.getAddressId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Set a Specific Address as Default for a User
    public boolean setDefaultAddress(int userId, int addressId) throws SQLException {
        resetDefaultAddressForUser(userId);

        String query = "UPDATE addresses SET is_default = 1 WHERE address_id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, addressId);
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 7. Delete Address
    public boolean deleteAddress(int addressId) throws SQLException {
        String query = "DELETE FROM addresses WHERE address_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, addressId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper: Reset all default address flags for a given user
    private void resetDefaultAddressForUser(int userId) throws SQLException {
        String query = "UPDATE addresses SET is_default = 0 WHERE user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }

    // Helper: Map ResultSet row to Address Object
    private Address mapResultSetToAddress(ResultSet rs) throws SQLException {
        Address addr = new Address();
        addr.setAddressId(rs.getInt("address_id"));
        addr.setUserId(rs.getInt("user_id"));
        addr.setAddressType(rs.getString("address_type"));
        addr.setAddressLine(rs.getString("address_line"));
        addr.setCity(rs.getString("city"));
        addr.setArea(rs.getString("area"));
        addr.setPostalCode(rs.getString("postal_code"));
        addr.setLatitude(rs.getBigDecimal("latitude"));
        addr.setLongitude(rs.getBigDecimal("longitude"));
        addr.setDefault(rs.getBoolean("is_default"));
        return addr;
    }
}