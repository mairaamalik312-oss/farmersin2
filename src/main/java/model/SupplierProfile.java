package model;

import java.math.BigDecimal;

public class SupplierProfile {
    private int supplierId;
    private int userId;
    private String supplierType;
    private String farmOrBusinessName;
    private String cnicNumber;
    private String registrationNumber; // Can be NULL
    private String verificationStatus;  // PENDING, VERIFIED, REJECTED
    private BigDecimal averageRating;
    private int totalCompletedOrders;

    // Default Constructor
    public SupplierProfile() {}

    // Constructor for registering a new supplier profile
    public SupplierProfile(int userId, String supplierType, String farmOrBusinessName,
                           String cnicNumber, String registrationNumber) {
        this.userId = userId;
        this.supplierType = supplierType;
        this.farmOrBusinessName = farmOrBusinessName;
        this.cnicNumber = cnicNumber;
        this.registrationNumber = registrationNumber;
        this.verificationStatus = "PENDING";
        this.averageRating = new BigDecimal("0.00");
        this.totalCompletedOrders = 0;
    }

    // Full Constructor
    public SupplierProfile(int supplierId, int userId, String supplierType,
                           String farmOrBusinessName, String cnicNumber,
                           String registrationNumber, String verificationStatus,
                           BigDecimal averageRating, int totalCompletedOrders) {
        this.supplierId = supplierId;
        this.userId = userId;
        this.supplierType = supplierType;
        this.farmOrBusinessName = farmOrBusinessName;
        this.cnicNumber = cnicNumber;
        this.registrationNumber = registrationNumber;
        this.verificationStatus = verificationStatus;
        this.averageRating = averageRating;
        this.totalCompletedOrders = totalCompletedOrders;
    }

    // Getters and Setters
    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getSupplierType() { return supplierType; }
    public void setSupplierType(String supplierType) { this.supplierType = supplierType; }

    public String getFarmOrBusinessName() { return farmOrBusinessName; }
    public void setFarmOrBusinessName(String farmOrBusinessName) { this.farmOrBusinessName = farmOrBusinessName; }

    public String getCnicNumber() { return cnicNumber; }
    public void setCnicNumber(String cnicNumber) { this.cnicNumber = cnicNumber; }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public BigDecimal getAverageRating() { return averageRating; }
    public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }

    public int getTotalCompletedOrders() { return totalCompletedOrders; }
    public void setTotalCompletedOrders(int totalCompletedOrders) { this.totalCompletedOrders = totalCompletedOrders; }
}
