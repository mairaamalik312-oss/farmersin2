package model;

import java.math.BigDecimal;

public class Address {
    private int addressId;
    private int userId;
    private String addressType; // BUSINESS, DELIVERY, FARM, BILLING
    private String addressLine;
    private String city;
    private String area;
    private String postalCode;  // Can be NULL
    private BigDecimal latitude; // Can be NULL
    private BigDecimal longitude; // Can be NULL
    private boolean isDefault;

    // Default Constructor
    public Address() {}

    // Constructor for creating a new address
    public Address(int userId, String addressType, String addressLine,
                   String city, String area, String postalCode,
                   BigDecimal latitude, BigDecimal longitude, boolean isDefault) {
        this.userId = userId;
        this.addressType = addressType;
        this.addressLine = addressLine;
        this.city = city;
        this.area = area;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isDefault = isDefault;
    }

    // Full Constructor
    public Address(int addressId, int userId, String addressType, String addressLine,
                   String city, String area, String postalCode,
                   BigDecimal latitude, BigDecimal longitude, boolean isDefault) {
        this.addressId = addressId;
        this.userId = userId;
        this.addressType = addressType;
        this.addressLine = addressLine;
        this.city = city;
        this.area = area;
        this.postalCode = postalCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.isDefault = isDefault;
    }

    // Getters and Setters
    public int getAddressId() { return addressId; }
    public void setAddressId(int addressId) { this.addressId = addressId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getAddressType() { return addressType; }
    public void setAddressType(String addressType) { this.addressType = addressType; }

    public String getAddressLine() { return addressLine; }
    public void setAddressLine(String addressLine) { this.addressLine = addressLine; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
}