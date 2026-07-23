package dao;

import database.DBConnection;
import model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class products {

    // 1. Add a New Product
    public boolean addProduct(Product product) throws SQLException {
        String query = "INSERT INTO products (product_name, category_id, subcategory_id, description, " +
                "default_unit, image_path, is_seasonal, is_active) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getProductName());
            stmt.setInt(2, product.getCategoryId());

            if (product.getSubcategoryId() != null) {
                stmt.setInt(3, product.getSubcategoryId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            if (product.getDescription() != null) {
                stmt.setString(4, product.getDescription());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            if (product.getDefaultUnit() != null) {
                stmt.setString(5, product.getDefaultUnit());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            if (product.getImagePath() != null) {
                stmt.setString(6, product.getImagePath());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            stmt.setBoolean(7, product.isSeasonal());
            stmt.setBoolean(8, product.isActive());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        product.setProductId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Product by ID
    public Product getProductById(int productId) throws SQLException {
        String query = "SELECT * FROM products WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        return null;
    }

    // 3. Get All Active Products
    public List<Product> getAllActiveProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products WHERE is_active = 1 ORDER BY product_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
        }
        return list;
    }

    // 4. Get Products by Category
    public List<Product> getProductsByCategoryId(int categoryId) throws SQLException {
        List<Product> list = new ArrayList<>();
        String query = "SELECT * FROM products WHERE category_id = ? AND is_active = 1 ORDER BY product_name ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        }
        return list;
    }

    // 5. Update Product Details
    public boolean updateProduct(Product product) throws SQLException {
        String query = "UPDATE products SET product_name = ?, category_id = ?, subcategory_id = ?, " +
                "description = ?, default_unit = ?, image_path = ?, is_seasonal = ?, is_active = ? " +
                "WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, product.getProductName());
            stmt.setInt(2, product.getCategoryId());

            if (product.getSubcategoryId() != null) {
                stmt.setInt(3, product.getSubcategoryId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            if (product.getDescription() != null) {
                stmt.setString(4, product.getDescription());
            } else {
                stmt.setNull(4, Types.VARCHAR);
            }

            if (product.getDefaultUnit() != null) {
                stmt.setString(5, product.getDefaultUnit());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            if (product.getImagePath() != null) {
                stmt.setString(6, product.getImagePath());
            } else {
                stmt.setNull(6, Types.VARCHAR);
            }

            stmt.setBoolean(7, product.isSeasonal());
            stmt.setBoolean(8, product.isActive());
            stmt.setInt(9, product.getProductId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Soft Delete / Toggle Active Status
    public boolean setProductActiveStatus(int productId, boolean isActive) throws SQLException {
        String query = "UPDATE products SET is_active = ? WHERE product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBoolean(1, isActive);
            stmt.setInt(2, productId);

            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to Product Object
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setProductName(rs.getString("product_name"));
        product.setCategoryId(rs.getInt("category_id"));

        int subcategoryId = rs.getInt("subcategory_id");
        product.setSubcategoryId(rs.wasNull() ? null : subcategoryId);

        product.setDescription(rs.getString("description"));
        product.setDefaultUnit(rs.getString("default_unit"));
        product.setImagePath(rs.getString("image_path"));
        product.setSeasonal(rs.getBoolean("is_seasonal"));
        product.setActive(rs.getBoolean("is_active"));

        return product;
    }
}