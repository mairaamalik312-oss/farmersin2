package dao;

import database.DBConnection;
import model.SupplierProduct;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class supplier_products {

    // 1. Add a New Supplier Product
    public boolean addSupplierProduct(SupplierProduct sp) throws SQLException {
        String query = "INSERT INTO supplier_products (supplier_id, product_id, price_per_unit, " +
                "available_quantity, minimum_order_quantity, unit_type, quality_grade, " +
                "production_or_harvest_date, expiry_date, listing_status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, sp.getSupplierId());
            stmt.setInt(2, sp.getProductId());
            stmt.setBigDecimal(3, sp.getPricePerUnit());
            stmt.setBigDecimal(4, sp.getAvailableQuantity());
            stmt.setBigDecimal(5, sp.getMinimumOrderQuantity());
            stmt.setString(6, sp.getUnitType());

            if (sp.getQualityGrade() != null) {
                stmt.setString(7, sp.getQualityGrade());
            } else {
                stmt.setNull(7, Types.VARCHAR);
            }

            if (sp.getProductionOrHarvestDate() != null) {
                stmt.setDate(8, sp.getProductionOrHarvestDate());
            } else {
                stmt.setNull(8, Types.DATE);
            }

            if (sp.getExpiryDate() != null) {
                stmt.setDate(9, sp.getExpiryDate());
            } else {
                stmt.setNull(9, Types.DATE);
            }

            stmt.setString(10, sp.getListingStatus() != null ? sp.getListingStatus() : "PENDING");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        sp.setSupplierProductId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Supplier Product by ID
    public SupplierProduct getSupplierProductById(int supplierProductId) throws SQLException {
        String query = "SELECT * FROM supplier_products WHERE supplier_product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, supplierProductId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSupplierProduct(rs);
                }
            }
        }
        return null;
    }

    // 3. Get All Approved Listings for a Specific Base Product (For Marketplace Display)
    public List<SupplierProduct> getApprovedByProductId(int productId) throws SQLException {
        List<SupplierProduct> list = new ArrayList<>();
        String query = "SELECT * FROM supplier_products WHERE product_id = ? AND listing_status = 'APPROVED' AND available_quantity > 0 ORDER BY price_per_unit ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSupplierProduct(rs));
                }
            }
        }
        return list;
    }

    // 4. Get All Listings by Supplier ID (For Supplier Dashboard)
    public List<SupplierProduct> getListingsBySupplierId(int supplierId) throws SQLException {
        List<SupplierProduct> list = new ArrayList<>();
        String query = "SELECT * FROM supplier_products WHERE supplier_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToSupplierProduct(rs));
                }
            }
        }
        return list;
    }

    // 5. Update Supplier Product Details
    public boolean updateSupplierProduct(SupplierProduct sp) throws SQLException {
        String query = "UPDATE supplier_products SET price_per_unit = ?, available_quantity = ?, " +
                "minimum_order_quantity = ?, unit_type = ?, quality_grade = ?, " +
                "production_or_harvest_date = ?, expiry_date = ?, listing_status = ? " +
                "WHERE supplier_product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setBigDecimal(1, sp.getPricePerUnit());
            stmt.setBigDecimal(2, sp.getAvailableQuantity());
            stmt.setBigDecimal(3, sp.getMinimumOrderQuantity());
            stmt.setString(4, sp.getUnitType());

            if (sp.getQualityGrade() != null) {
                stmt.setString(5, sp.getQualityGrade());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            if (sp.getProductionOrHarvestDate() != null) {
                stmt.setDate(6, sp.getProductionOrHarvestDate());
            } else {
                stmt.setNull(6, Types.DATE);
            }

            if (sp.getExpiryDate() != null) {
                stmt.setDate(7, sp.getExpiryDate());
            } else {
                stmt.setNull(7, Types.DATE);
            }

            stmt.setString(8, sp.getListingStatus());
            stmt.setInt(9, sp.getSupplierProductId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 6. Update Listing Status (Admin Approval Workflow)
    public boolean updateListingStatus(int supplierProductId, String status) throws SQLException {
        String query = "UPDATE supplier_products SET listing_status = ? WHERE supplier_product_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status);
            stmt.setInt(2, supplierProductId);

            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to SupplierProduct Object
    private SupplierProduct mapResultSetToSupplierProduct(ResultSet rs) throws SQLException {
        SupplierProduct sp = new SupplierProduct();
        sp.setSupplierProductId(rs.getInt("supplier_product_id"));
        sp.setSupplierId(rs.getInt("supplier_id"));
        sp.setProductId(rs.getInt("product_id"));
        sp.setPricePerUnit(rs.getBigDecimal("price_per_unit"));
        sp.setAvailableQuantity(rs.getBigDecimal("available_quantity"));
        sp.setMinimumOrderQuantity(rs.getBigDecimal("minimum_order_quantity"));
        sp.setUnitType(rs.getString("unit_type"));
        sp.setQualityGrade(rs.getString("quality_grade"));
        sp.setProductionOrHarvestDate(rs.getDate("production_or_harvest_date"));
        sp.setExpiryDate(rs.getDate("expiry_date"));
        sp.setListingStatus(rs.getString("listing_status"));
        sp.setCreatedAt(rs.getTimestamp("created_at"));
        sp.setUpdatedAt(rs.getTimestamp("updated_at"));
        return sp;
    }
}
