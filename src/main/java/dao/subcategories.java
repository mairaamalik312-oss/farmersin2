package dao;

import database.DBConnection;
import model.Subcategory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class subcategories {

    // 1. Add a New Subcategory
    public boolean addSubcategory(Subcategory subcategory) throws SQLException {
        String query = "INSERT INTO subcategories (category_id, subcategory_name, is_active) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, subcategory.getCategoryId());
            stmt.setString(2, subcategory.getSubcategoryName());
            stmt.setBoolean(3, subcategory.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        subcategory.setSubcategoryId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Subcategory by ID
    public Subcategory getSubcategoryById(int subcategoryId) throws SQLException {
        String query = "SELECT * FROM subcategories WHERE subcategory_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, subcategoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSubcategory(rs);
                }
            }
        }
        return null;
    }

    // 3. Get All Subcategories by Parent Category ID
    public List<Subcategory> getSubcategoriesByCategoryId(int categoryId) throws SQLException {
        List<Subcategory> list = new ArrayList<>();
        String query = "SELECT * FROM subcategories WHERE category_id = ? AND is_active = 1 ORDER BY subcategory_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSubcategory(rs));
                }
            }
        }
        return list;
    }

    // 4. Get All Active Subcategories
    public List<Subcategory> getAllActiveSubcategories() throws SQLException {
        List<Subcategory> list = new ArrayList<>();
        String query = "SELECT * FROM subcategories WHERE is_active = 1 ORDER BY subcategory_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToSubcategory(rs));
            }
        }
        return list;
    }

    // 5. Update Subcategory
    public boolean updateSubcategory(Subcategory subcategory) throws SQLException {
        String query = "UPDATE subcategories SET category_id = ?, subcategory_name = ?, is_active = ? WHERE subcategory_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, subcategory.getCategoryId());
            stmt.setString(2, subcategory.getSubcategoryName());
            stmt.setBoolean(3, subcategory.isActive());
            stmt.setInt(4, subcategory.getSubcategoryId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Toggle Active Status / Soft Delete
    public boolean setSubcategoryActiveStatus(int subcategoryId, boolean isActive) throws SQLException {
        String query = "UPDATE subcategories SET is_active = ? WHERE subcategory_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, isActive);
            stmt.setInt(2, subcategoryId);

            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to Subcategory Object
    private Subcategory mapResultSetToSubcategory(ResultSet rs) throws SQLException {
        Subcategory subcategory = new Subcategory();
        subcategory.setSubcategoryId(rs.getInt("subcategory_id"));
        subcategory.setCategoryId(rs.getInt("category_id"));
        subcategory.setSubcategoryName(rs.getString("subcategory_name"));
        subcategory.setActive(rs.getBoolean("is_active"));
        return subcategory;
    }
}