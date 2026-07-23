package dao;

import database.DatabaseConnection; // Adjust to your actual connection package
import model.Category;               // Adjust to your actual model package

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class categories {

    // 1. Create a Category
    public boolean addCategory(Category category) throws SQLException {
        String query = "INSERT INTO categories (category_name, description, is_active) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
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

    // 2. Get Category by ID
    public Category getCategoryById(int categoryId) throws SQLException {
        String query = "SELECT * FROM categories WHERE category_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
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

    // 3. Get All Categories
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categoryList = new ArrayList<>();
        String query = "SELECT * FROM categories";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                categoryList.add(mapResultSetToCategory(rs));
            }
        }
        return categoryList;
    }

    // 4. Get Only Active Categories (Useful for buyer store views)
    public List<Category> getActiveCategories() throws SQLException {
        List<Category> categoryList = new ArrayList<>();
        String query = "SELECT * FROM categories WHERE is_active = 1";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                categoryList.add(mapResultSetToCategory(rs));
            }
        }
        return categoryList;
    }

    // 5. Update Category
    public boolean updateCategory(Category category) throws SQLException {
        String query = "UPDATE categories SET category_name = ?, description = ?, is_active = ? WHERE category_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, category.getCategoryName());
            stmt.setString(2, category.getDescription());
            stmt.setBoolean(3, category.isActive());
            stmt.setInt(4, category.getCategoryId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Delete Category
    public boolean deleteCategory(int categoryId) throws SQLException {
        String query = "DELETE FROM categories WHERE category_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to convert ResultSet row to Category Object
    private Category mapResultSetToCategory(ResultSet rs) throws SQLException {
        Category category = new Category();
        category.setCategoryId(rs.getInt("category_id"));
        category.setCategoryName(rs.getString("category_name"));
        category.setDescription(rs.getString("description"));
        category.setActive(rs.getBoolean("is_active"));
        return category;
    }
}
