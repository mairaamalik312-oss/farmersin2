package model;

public class BuyerProfile {
    private int buyerId;
    private int userId;
    private String businessName;
    private String businessType;
    private String registrationNumber; // Can be NULL
    private String taxNumber;          // Can be NULL
    private String verificationStatus;  // PENDING, VERIFIED, REJECTED

    // Default Constructor
    public BuyerProfile() {}

    // Constructor for registering a new buyer profile
    public BuyerProfile(int userId, String businessName, String businessType,
                        String registrationNumber, String taxNumber) {
        this.userId = userId;
        this.businessName = businessName;
        this.businessType = businessType;
        this.registrationNumber = registrationNumber;
        this.taxNumber = taxNumber;
        this.verificationStatus = "PENDING";
    }

    // Full Constructor
    public BuyerProfile(int buyerId, int userId, String businessName,
                        String businessType, String registrationNumber,
                        String taxNumber, String verificationStatus) {
        this.buyerId = buyerId;
        this.userId = userId;
        this.businessName = businessName;
        this.businessType = businessType;
        this.registrationNumber = registrationNumber;
        this.taxNumber = taxNumber;
        this.verificationStatus = verificationStatus;
    }

    // Getters and Setters
    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getBusinessType() { return businessType; }
    public void setBusinessType(String businessType) { this.businessType = businessType; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getTaxNumber() { return taxNumber; }
    public void setTaxNumber(String taxNumber) { this.taxNumber = taxNumber; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }
}
