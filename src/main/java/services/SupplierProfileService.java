package services;

import dao.supplier_profiles;
import dao.supplier_products;

import model.SupplierProfile;
import model.SupplierProduct;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class SupplierProfileService {

    private final supplier_profiles supplierProfileDAO;
    private final supplier_products supplierProductDAO;

    // =========================================================
    // DEFAULT CONSTRUCTOR
    // =========================================================
    public SupplierProfileService() {
        this.supplierProfileDAO = new supplier_profiles();
        this.supplierProductDAO = new supplier_products();
    }

    // =========================================================
    // CONSTRUCTOR FOR TESTING / DEPENDENCY INJECTION
    // =========================================================
    public SupplierProfileService(
            supplier_profiles supplierProfileDAO,
            supplier_products supplierProductDAO
    ) {

        if (supplierProfileDAO == null) {
            throw new IllegalArgumentException(
                    "Supplier profile DAO cannot be null."
            );
        }

        if (supplierProductDAO == null) {
            throw new IllegalArgumentException(
                    "Supplier product DAO cannot be null."
            );
        }

        this.supplierProfileDAO = supplierProfileDAO;
        this.supplierProductDAO = supplierProductDAO;
    }

    // =========================================================
    // ADD SUPPLIER PROFILE
    // =========================================================
    public boolean addSupplierProfile(
            SupplierProfile profile
    ) throws SQLException {

        validateSupplierProfile(profile);
        cleanSupplierProfile(profile);

        if (supplierProfileDAO.getSupplierByUserId(
                profile.getUserId()
        ) != null) {

            throw new IllegalArgumentException(
                    "Supplier profile already exists for this user."
            );
        }

        profile.setVerificationStatus("PENDING");
        profile.setAverageRating(BigDecimal.ZERO);
        profile.setTotalCompletedOrders(0);

        return supplierProfileDAO.addSupplierProfile(profile);
    }

    // =========================================================
    // ADD PRODUCT
    // =========================================================
    public boolean addProduct(
            SupplierProduct product
    ) throws SQLException {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product cannot be null."
            );
        }

        // Validate Supplier ID
        validateSupplierId(product.getSupplierId());

        // Check if supplier exists
        SupplierProfile supplier =
                supplierProfileDAO.getSupplierById(
                        product.getSupplierId()
                );

        if (supplier == null) {
            throw new IllegalArgumentException(
                    "Supplier profile not found."
            );
        }

        // Supplier must be verified
        if (!"VERIFIED".equalsIgnoreCase(
                supplier.getVerificationStatus()
        )) {

            throw new IllegalArgumentException(
                    "Supplier must be verified before adding products."
            );
        }

        // Validate Product ID
        if (product.getProductId() <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be greater than zero."
            );
        }

        // Validate Price
        if (product.getPricePerUnit() == null
                || product.getPricePerUnit()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Price per unit must be greater than zero."
            );
        }

        // Validate Available Quantity
        if (product.getAvailableQuantity() == null
                || product.getAvailableQuantity()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Available quantity cannot be negative."
            );
        }

        // Validate Minimum Order Quantity
        if (product.getMinimumOrderQuantity() == null
                || product.getMinimumOrderQuantity()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Minimum order quantity must be greater than zero."
            );
        }

        // Minimum order cannot exceed available stock
        if (product.getMinimumOrderQuantity()
                .compareTo(
                        product.getAvailableQuantity()
                ) > 0) {

            throw new IllegalArgumentException(
                    "Minimum order quantity cannot exceed available quantity."
            );
        }

        // Validate Unit Type
        if (isBlank(product.getUnitType())) {
            throw new IllegalArgumentException(
                    "Unit type is required."
            );
        }

        // Clean Unit Type
        product.setUnitType(
                product.getUnitType()
                        .trim()
                        .toUpperCase()
        );

        // Clean Quality Grade
        if (product.getQualityGrade() != null) {

            String qualityGrade =
                    product.getQualityGrade().trim();

            product.setQualityGrade(
                    qualityGrade.isEmpty()
                            ? null
                            : qualityGrade.toUpperCase()
            );
        }

        // Validate Dates
        if (product.getProductionOrHarvestDate() != null
                && product.getExpiryDate() != null
                && product.getExpiryDate().before(
                product.getProductionOrHarvestDate()
        )) {

            throw new IllegalArgumentException(
                    "Expiry date cannot be before production or harvest date."
            );
        }

        // New product listing starts as pending
        product.setListingStatus("PENDING");

        return supplierProductDAO.addSupplierProduct(
                product
        );
    }

    // =========================================================
    // GET SUPPLIER BY SUPPLIER ID
    // =========================================================
    public SupplierProfile getSupplierById(
            int supplierId
    ) throws SQLException {

        validateSupplierId(supplierId);

        SupplierProfile profile =
                supplierProfileDAO.getSupplierById(
                        supplierId
                );

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Supplier profile not found."
            );
        }

        return profile;
    }

    // =========================================================
    // GET SUPPLIER BY USER ID
    // =========================================================
    public SupplierProfile getSupplierByUserId(
            int userId
    ) throws SQLException {

        validateUserId(userId);

        SupplierProfile profile =
                supplierProfileDAO.getSupplierByUserId(
                        userId
                );

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Supplier profile not found."
            );
        }

        return profile;
    }

    // =========================================================
    // UPDATE SUPPLIER PROFILE
    // =========================================================
    public boolean updateSupplierProfile(
            SupplierProfile profile
    ) throws SQLException {

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Supplier profile cannot be null."
            );
        }

        validateSupplierId(
                profile.getSupplierId()
        );

        validateSupplierProfile(profile);

        cleanSupplierProfile(profile);

        if (supplierProfileDAO.getSupplierById(
                profile.getSupplierId()
        ) == null) {

            throw new IllegalArgumentException(
                    "Supplier profile not found."
            );
        }

        return supplierProfileDAO.updateSupplierProfile(
                profile
        );
    }

    // =========================================================
    // UPDATE SUPPLIER VERIFICATION STATUS
    // =========================================================
    public boolean updateVerificationStatus(
            int supplierId,
            String status
    ) throws SQLException {

        validateSupplierId(supplierId);

        String formattedStatus =
                validateAndFormatStatus(status);

        if (supplierProfileDAO.getSupplierById(
                supplierId
        ) == null) {

            throw new IllegalArgumentException(
                    "Supplier profile not found."
            );
        }

        return supplierProfileDAO.updateVerificationStatus(
                supplierId,
                formattedStatus
        );
    }

    // =========================================================
    // UPDATE SUPPLIER STATS
    // =========================================================
    public boolean updateSupplierStats(
            int supplierId,
            BigDecimal averageRating,
            int incrementCompletedOrders
    ) throws SQLException {

        validateSupplierId(supplierId);

        if (averageRating == null
                || averageRating.compareTo(
                BigDecimal.ZERO
        ) < 0
                || averageRating.compareTo(
                new BigDecimal("5")
        ) > 0) {

            throw new IllegalArgumentException(
                    "Average rating must be between 0 and 5."
            );
        }

        if (incrementCompletedOrders < 0) {
            throw new IllegalArgumentException(
                    "Completed order increment cannot be negative."
            );
        }

        if (supplierProfileDAO.getSupplierById(
                supplierId
        ) == null) {

            throw new IllegalArgumentException(
                    "Supplier profile not found."
            );
        }

        return supplierProfileDAO.updateSupplierStats(
                supplierId,
                averageRating,
                incrementCompletedOrders
        );
    }

    // =========================================================
    // GET PENDING SUPPLIER VERIFICATIONS
    // =========================================================
    public List<SupplierProfile> getPendingVerifications()
            throws SQLException {

        return supplierProfileDAO
                .getPendingVerifications();
    }

    // =========================================================
    // VALIDATE SUPPLIER PROFILE
    // =========================================================
    private void validateSupplierProfile(
            SupplierProfile profile
    ) {

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Supplier profile cannot be null."
            );
        }

        validateUserId(
                profile.getUserId()
        );

        if (isBlank(profile.getSupplierType())) {
            throw new IllegalArgumentException(
                    "Supplier type is required."
            );
        }

        if (isBlank(
                profile.getFarmOrBusinessName()
        )) {

            throw new IllegalArgumentException(
                    "Farm or business name is required."
            );
        }

        if (isBlank(profile.getCnicNumber())) {
            throw new IllegalArgumentException(
                    "CNIC number is required."
            );
        }
    }

    // =========================================================
    // CLEAN SUPPLIER PROFILE
    // =========================================================
    private void cleanSupplierProfile(
            SupplierProfile profile
    ) {

        profile.setSupplierType(
                profile.getSupplierType()
                        .trim()
                        .toUpperCase()
        );

        profile.setFarmOrBusinessName(
                profile.getFarmOrBusinessName()
                        .trim()
        );

        profile.setCnicNumber(
                profile.getCnicNumber()
                        .trim()
        );

        if (profile.getRegistrationNumber() != null) {

            String registrationNumber =
                    profile.getRegistrationNumber()
                            .trim();

            profile.setRegistrationNumber(
                    registrationNumber.isEmpty()
                            ? null
                            : registrationNumber
            );
        }
    }

    // =========================================================
    // VALIDATE VERIFICATION STATUS
    // =========================================================
    private String validateAndFormatStatus(
            String status
    ) {

        if (isBlank(status)) {
            throw new IllegalArgumentException(
                    "Verification status is required."
            );
        }

        String formatted =
                status.trim().toUpperCase();

        if (!formatted.equals("PENDING")
                && !formatted.equals("VERIFIED")
                && !formatted.equals("REJECTED")) {

            throw new IllegalArgumentException(
                    "Invalid verification status."
            );
        }

        return formatted;
    }

    // =========================================================
    // VALIDATE SUPPLIER ID
    // =========================================================
    private void validateSupplierId(
            int supplierId
    ) {

        if (supplierId <= 0) {
            throw new IllegalArgumentException(
                    "Supplier ID must be greater than zero."
            );
        }
    }

    // =========================================================
    // VALIDATE USER ID
    // =========================================================
    private void validateUserId(
            int userId
    ) {

        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be greater than zero."
            );
        }
    }

    // =========================================================
    // CHECK BLANK STRING
    // =========================================================
    private boolean isBlank(
            String value
    ) {

        return value == null
                || value.trim().isEmpty();
    }
}