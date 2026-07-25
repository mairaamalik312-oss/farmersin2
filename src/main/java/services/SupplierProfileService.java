package services;

import dao.supplier_profiles;
import model.SupplierProfile;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class SupplierProfileService {

    private final supplier_profiles supplierProfileDAO;

    public SupplierProfileService() {
        this.supplierProfileDAO = new supplier_profiles();
    }

    public SupplierProfileService(
            supplier_profiles supplierProfileDAO
    ) {
        if (supplierProfileDAO == null) {
            throw new IllegalArgumentException(
                    "Supplier profile DAO cannot be null."
            );
        }
        this.supplierProfileDAO = supplierProfileDAO;
    }

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

    public SupplierProfile getSupplierById(int supplierId)
            throws SQLException {

        validateSupplierId(supplierId);

        SupplierProfile profile =
                supplierProfileDAO.getSupplierById(supplierId);

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Supplier profile not found."
            );
        }

        return profile;
    }

    public SupplierProfile getSupplierByUserId(int userId)
            throws SQLException {

        validateUserId(userId);

        SupplierProfile profile =
                supplierProfileDAO.getSupplierByUserId(userId);

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Supplier profile not found."
            );
        }

        return profile;
    }

    public boolean updateSupplierProfile(
            SupplierProfile profile
    ) throws SQLException {

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Supplier profile cannot be null."
            );
        }

        validateSupplierId(profile.getSupplierId());
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

    public boolean updateSupplierStats(
            int supplierId,
            BigDecimal averageRating,
            int incrementCompletedOrders
    ) throws SQLException {

        validateSupplierId(supplierId);

        if (averageRating == null
                || averageRating.compareTo(BigDecimal.ZERO) < 0
                || averageRating.compareTo(new BigDecimal("5")) > 0) {
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

    public List<SupplierProfile> getPendingVerifications()
            throws SQLException {

        return supplierProfileDAO.getPendingVerifications();
    }

    private void validateSupplierProfile(
            SupplierProfile profile
    ) {
        if (profile == null) {
            throw new IllegalArgumentException(
                    "Supplier profile cannot be null."
            );
        }

        validateUserId(profile.getUserId());

        if (isBlank(profile.getSupplierType())) {
            throw new IllegalArgumentException(
                    "Supplier type is required."
            );
        }

        if (isBlank(profile.getFarmOrBusinessName())) {
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

    private void cleanSupplierProfile(
            SupplierProfile profile
    ) {
        profile.setSupplierType(
                profile.getSupplierType()
                        .trim()
                        .toUpperCase()
        );

        profile.setFarmOrBusinessName(
                profile.getFarmOrBusinessName().trim()
        );

        profile.setCnicNumber(
                profile.getCnicNumber().trim()
        );

        if (profile.getRegistrationNumber() != null) {
            String value =
                    profile.getRegistrationNumber().trim();

            profile.setRegistrationNumber(
                    value.isEmpty() ? null : value
            );
        }
    }

    private String validateAndFormatStatus(String status) {
        if (isBlank(status)) {
            throw new IllegalArgumentException(
                    "Verification status is required."
            );
        }

        String formatted = status.trim().toUpperCase();

        if (!formatted.equals("PENDING")
                && !formatted.equals("VERIFIED")
                && !formatted.equals("REJECTED")) {
            throw new IllegalArgumentException(
                    "Invalid verification status."
            );
        }

        return formatted;
    }

    private void validateSupplierId(int supplierId) {
        if (supplierId <= 0) {
            throw new IllegalArgumentException(
                    "Supplier ID must be greater than zero."
            );
        }
    }

    private void validateUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be greater than zero."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}