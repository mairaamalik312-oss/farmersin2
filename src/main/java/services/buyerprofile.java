package services;

import dao.buyer_profiles;
import model.BuyerProfile;

import java.sql.SQLException;
import java.util.List;

public class buyerprofile {

    private final buyer_profiles buyerProfileDAO;

    // Default constructor
    public buyerprofile() {
        this.buyerProfileDAO = new buyer_profiles();
    }

    // Constructor useful for testing
    public buyerprofile(buyer_profiles buyerProfileDAO) {
        if (buyerProfileDAO == null) {
            throw new IllegalArgumentException(
                    "Buyer profile DAO cannot be null."
            );
        }

        this.buyerProfileDAO = buyerProfileDAO;
    }

    // 1. Add a new buyer profile
    public boolean addBuyerProfile(BuyerProfile profile)
            throws SQLException {

        validateBuyerProfile(profile);

        /*
         * One user should have only one buyer profile.
         */
        BuyerProfile existingProfile =
                buyerProfileDAO.getBuyerByUserId(profile.getUserId());

        if (existingProfile != null) {
            throw new IllegalArgumentException(
                    "A buyer profile already exists for user ID "
                            + profile.getUserId() + "."
            );
        }

        cleanProfileData(profile);

        /*
         * Every newly created buyer profile starts
         * with PENDING verification status.
         */
        profile.setVerificationStatus("PENDING");

        return buyerProfileDAO.addBuyerProfile(profile);
    }

    // 2. Get buyer profile by buyer ID
    public BuyerProfile getBuyerById(int buyerId)
            throws SQLException {

        validateBuyerId(buyerId);

        BuyerProfile profile =
                buyerProfileDAO.getBuyerById(buyerId);

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Buyer profile with ID "
                            + buyerId + " was not found."
            );
        }

        return profile;
    }

    // 3. Get buyer profile by user ID
    public BuyerProfile getBuyerByUserId(int userId)
            throws SQLException {

        validateUserId(userId);

        BuyerProfile profile =
                buyerProfileDAO.getBuyerByUserId(userId);

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Buyer profile for user ID "
                            + userId + " was not found."
            );
        }

        return profile;
    }

    // 4. Update an existing buyer profile
    public boolean updateBuyerProfile(BuyerProfile profile)
            throws SQLException {

        if (profile == null) {
            throw new IllegalArgumentException(
                    "Buyer profile cannot be null."
            );
        }

        validateBuyerId(profile.getBuyerId());
        validateBuyerProfile(profile);

        BuyerProfile existingProfile =
                buyerProfileDAO.getBuyerById(profile.getBuyerId());

        if (existingProfile == null) {
            throw new IllegalArgumentException(
                    "Buyer profile with ID "
                            + profile.getBuyerId()
                            + " was not found."
            );
        }

        /*
         * Prevent changing the user who owns
         * the buyer profile.
         */
        if (existingProfile.getUserId() != profile.getUserId()) {
            throw new IllegalArgumentException(
                    "This buyer profile does not belong to the specified user."
            );
        }

        cleanProfileData(profile);

        return buyerProfileDAO.updateBuyerProfile(profile);
    }

    // 5. Update verification status
    public boolean updateVerificationStatus(
            int buyerId,
            String status
    ) throws SQLException {

        validateBuyerId(buyerId);

        String formattedStatus =
                validateAndFormatVerificationStatus(status);

        BuyerProfile existingProfile =
                buyerProfileDAO.getBuyerById(buyerId);

        if (existingProfile == null) {
            throw new IllegalArgumentException(
                    "Buyer profile with ID "
                            + buyerId + " was not found."
            );
        }

        /*
         * If the profile already has the requested status,
         * no database update is necessary.
         */
        if (formattedStatus.equalsIgnoreCase(
                existingProfile.getVerificationStatus()
        )) {
            return true;
        }

        return buyerProfileDAO.updateVerificationStatus(
                buyerId,
                formattedStatus
        );
    }

    // 6. Get all pending verification requests
    public List<BuyerProfile> getPendingVerifications()
            throws SQLException {

        return buyerProfileDAO.getPendingVerifications();
    }

    // Validate complete buyer profile data
    private void validateBuyerProfile(BuyerProfile profile) {
        if (profile == null) {
            throw new IllegalArgumentException(
                    "Buyer profile cannot be null."
            );
        }

        validateUserId(profile.getUserId());

        if (isBlank(profile.getBusinessName())) {
            throw new IllegalArgumentException(
                    "Business name is required."
            );
        }

        if (isBlank(profile.getBusinessType())) {
            throw new IllegalArgumentException(
                    "Business type is required."
            );
        }

        /*
         * These limits should match the VARCHAR sizes
         * used in your buyer_profiles table.
         */
        if (profile.getBusinessName().trim().length() > 150) {
            throw new IllegalArgumentException(
                    "Business name cannot exceed 150 characters."
            );
        }

        if (profile.getBusinessType().trim().length() > 100) {
            throw new IllegalArgumentException(
                    "Business type cannot exceed 100 characters."
            );
        }

        if (profile.getRegistrationNumber() != null &&
                profile.getRegistrationNumber().trim().length() > 100) {

            throw new IllegalArgumentException(
                    "Registration number cannot exceed 100 characters."
            );
        }

        if (profile.getTaxNumber() != null &&
                profile.getTaxNumber().trim().length() > 100) {

            throw new IllegalArgumentException(
                    "Tax number cannot exceed 100 characters."
            );
        }
    }

    // Remove extra spaces and standardize values
    private void cleanProfileData(BuyerProfile profile) {
        profile.setBusinessName(
                profile.getBusinessName().trim()
        );

        profile.setBusinessType(
                profile.getBusinessType().trim().toUpperCase()
        );

        profile.setRegistrationNumber(
                cleanOptionalValue(
                        profile.getRegistrationNumber()
                )
        );

        profile.setTaxNumber(
                cleanOptionalValue(
                        profile.getTaxNumber()
                )
        );
    }

    // Validate verification status
    private String validateAndFormatVerificationStatus(
            String status
    ) {
        if (isBlank(status)) {
            throw new IllegalArgumentException(
                    "Verification status is required."
            );
        }

        String formattedStatus =
                status.trim().toUpperCase();

        if (!formattedStatus.equals("PENDING") &&
                !formattedStatus.equals("VERIFIED") &&
                !formattedStatus.equals("REJECTED")) {

            throw new IllegalArgumentException(
                    "Verification status must be PENDING, VERIFIED, or REJECTED."
            );
        }

        return formattedStatus;
    }

    // Validate buyer ID
    private void validateBuyerId(int buyerId) {
        if (buyerId <= 0) {
            throw new IllegalArgumentException(
                    "Buyer ID must be greater than zero."
            );
        }
    }

    // Validate user ID
    private void validateUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be greater than zero."
            );
        }
    }

    // Clean optional String values
    private String cleanOptionalValue(String value) {
        if (value == null) {
            return null;
        }

        String cleanedValue = value.trim();

        return cleanedValue.isEmpty()
                ? null
                : cleanedValue;
    }

    // Check whether a String is null, empty, or spaces only
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}