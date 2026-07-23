package dao;

import database.DBConnection;
import model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class categories {

    // 1. Add a New Category
    public boolean addCategory(Category category) throws SQLException {
        String query = "INSERT INTO categories (category_name, description, is_active) VALUES (?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getCategoryName());

            if (category.getDescription() != null) {
                stmt.setString(2, category.getDescription());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            stmt.setBoolean(3, category.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        category.setCategoryId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Category by Category ID
    public Category getCategoryById(int categoryId) throws SQLException {
        String query = "SELECT * FROM categories WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        }
        return null;
    }

    // 3. Get Category by Name (Unique Key)
    public Category getCategoryByName(String categoryName) throws SQLException {
        String query = "SELECT * FROM categories WHERE category_name = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, categoryName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCategory(rs);
                }
            }
        }
        return null;
    }

    // 4. Get All Active Categories (Primary listing for buyers)
    public List<Category> getActiveCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String query = "SELECT * FROM categories WHERE is_active = 1 ORDER BY category_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToCategory(rs));
            }
        }
        return list;
    }

    // 5. Get All Categories (Active and Inactive - for Admin panel)
    public List<Category> getAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String query = "SELECT * FROM categories ORDER BY category_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToCategory(rs));
            }
        }
        return list;
    }

    // 6. Update Category Information
    public boolean updateCategory(Category category) throws SQLException {
        String query = "UPDATE categories SET category_name = ?, description = ?, is_active = ? WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, category.getCategoryName());

            if (category.getDescription() != null) {
                stmt.setString(2, category.getDescription());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            stmt.setBoolean(3, category.isActive());
            stmt.setInt(4, category.getCategoryId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 7. Toggle Active Status (Soft Disable/Enable)
    public boolean setCategoryActiveStatus(int categoryId, boolean isActive) throws SQLException {
        String query = "UPDATE categories SET is_active = ? WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, isActive);
            stmt.setInt(2, categoryId);

            return stmt.executeUpdate() > 0;
        }
    }

    // 8. Delete Category
    public boolean deleteCategory(int categoryId) throws SQLException {
        String query = "DELETE FROM categories WHERE category_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to Category Object
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setDescription(rs.getString("description"));
        category.setActive(rs.getBoolean("is_active"));
        return category;
    }
}