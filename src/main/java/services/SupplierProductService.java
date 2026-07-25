package services;

import dao.supplier_products;
import model.SupplierProduct;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class SupplierProductService {

    private final supplier_products supplierProductDAO;

    public SupplierProductService() {
        this.supplierProductDAO = new supplier_products();
    }

    public SupplierProductService(
            supplier_products supplierProductDAO
    ) {
        if (supplierProductDAO == null) {
            throw new IllegalArgumentException(
                    "Supplier product DAO cannot be null."
            );
        }
        this.supplierProductDAO = supplierProductDAO;
    }

    public boolean addSupplierProduct(SupplierProduct product)
            throws SQLException {

        validateSupplierProduct(product);
        cleanSupplierProduct(product);
        product.setListingStatus("PENDING");

        return supplierProductDAO.addSupplierProduct(product);
    }

    public SupplierProduct getSupplierProductById(
            int supplierProductId
    ) throws SQLException {

        validateSupplierProductId(supplierProductId);

        SupplierProduct product =
                supplierProductDAO.getSupplierProductById(
                        supplierProductId
                );

        if (product == null) {
            throw new IllegalArgumentException(
                    "Supplier product not found."
            );
        }

        return product;
    }

    public List<SupplierProduct> getApprovedByProductId(
            int productId
    ) throws SQLException {

        validateProductId(productId);

        return supplierProductDAO.getApprovedByProductId(
                productId
        );
    }

    public List<SupplierProduct> getListingsBySupplierId(
            int supplierId
    ) throws SQLException {

        validateSupplierId(supplierId);

        return supplierProductDAO.getListingsBySupplierId(
                supplierId
        );
    }

    public boolean updateSupplierProduct(
            SupplierProduct product
    ) throws SQLException {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Supplier product cannot be null."
            );
        }

        validateSupplierProductId(
                product.getSupplierProductId()
        );

        validateSupplierProduct(product);
        cleanSupplierProduct(product);

        if (supplierProductDAO.getSupplierProductById(
                product.getSupplierProductId()
        ) == null) {
            throw new IllegalArgumentException(
                    "Supplier product not found."
            );
        }

        if (isBlank(product.getListingStatus())) {
            product.setListingStatus("PENDING");
        } else {
            product.setListingStatus(
                    validateAndFormatStatus(
                            product.getListingStatus()
                    )
            );
        }

        return supplierProductDAO.updateSupplierProduct(
                product
        );
    }

    public boolean updateListingStatus(
            int supplierProductId,
            String status
    ) throws SQLException {

        validateSupplierProductId(supplierProductId);

        String formattedStatus =
                validateAndFormatStatus(status);

        if (supplierProductDAO.getSupplierProductById(
                supplierProductId
        ) == null) {
            throw new IllegalArgumentException(
                    "Supplier product not found."
            );
        }

        return supplierProductDAO.updateListingStatus(
                supplierProductId,
                formattedStatus
        );
    }

    private void validateSupplierProduct(
            SupplierProduct product
    ) {
        if (product == null) {
            throw new IllegalArgumentException(
                    "Supplier product cannot be null."
            );
        }

        validateSupplierId(product.getSupplierId());
        validateProductId(product.getProductId());

        validatePositive(
                product.getPricePerUnit(),
                "Price per unit"
        );

        validateNonNegative(
                product.getAvailableQuantity(),
                "Available quantity"
        );

        validatePositive(
                product.getMinimumOrderQuantity(),
                "Minimum order quantity"
        );

        if (product.getMinimumOrderQuantity()
                .compareTo(product.getAvailableQuantity()) > 0) {
            throw new IllegalArgumentException(
                    "Minimum order quantity cannot exceed available quantity."
            );
        }

        if (isBlank(product.getUnitType())) {
            throw new IllegalArgumentException(
                    "Unit type is required."
            );
        }

        if (product.getProductionOrHarvestDate() != null
                && product.getExpiryDate() != null
                && product.getExpiryDate().before(
                product.getProductionOrHarvestDate()
        )) {
            throw new IllegalArgumentException(
                    "Expiry date cannot be before production date."
            );
        }
    }

    private void cleanSupplierProduct(
            SupplierProduct product
    ) {
        product.setUnitType(
                product.getUnitType().trim().toUpperCase()
        );

        if (product.getQualityGrade() != null) {
            String grade = product.getQualityGrade().trim();

            product.setQualityGrade(
                    grade.isEmpty()
                            ? null
                            : grade.toUpperCase()
            );
        }
    }

    private String validateAndFormatStatus(String status) {
        if (isBlank(status)) {
            throw new IllegalArgumentException(
                    "Listing status is required."
            );
        }

        String formatted = status.trim().toUpperCase();

        if (!formatted.equals("PENDING")
                && !formatted.equals("APPROVED")
                && !formatted.equals("REJECTED")
                && !formatted.equals("UNAVAILABLE")) {
            throw new IllegalArgumentException(
                    "Invalid listing status."
            );
        }

        return formatted;
    }

    private void validatePositive(
            BigDecimal value,
            String fieldName
    ) {
        if (value == null
                || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than zero."
            );
        }
    }

    private void validateNonNegative(
            BigDecimal value,
            String fieldName
    ) {
        if (value == null
                || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative."
            );
        }
    }

    private void validateSupplierProductId(
            int supplierProductId
    ) {
        if (supplierProductId <= 0) {
            throw new IllegalArgumentException(
                    "Supplier product ID must be greater than zero."
            );
        }
    }

    private void validateSupplierId(int supplierId) {
        if (supplierId <= 0) {
            throw new IllegalArgumentException(
                    "Supplier ID must be greater than zero."
            );
        }
    }

    private void validateProductId(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be greater than zero."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}