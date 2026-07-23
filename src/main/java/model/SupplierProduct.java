package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class SupplierProduct {
    private int supplierProductId;
    private int supplierId;
    private int productId;
    private BigDecimal pricePerUnit;
    private BigDecimal availableQuantity;
    private BigDecimal minimumOrderQuantity;
    private String unitType;
    private String qualityGrade;
    private Date productionOrHarvestDate; // Can be NULL
    private Date expiryDate;               // Can be NULL
    private String listingStatus;          // PENDING, APPROVED, REJECTED, UNAVAILABLE
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Default Constructor
    public SupplierProduct() {}

    // Constructor for creating a new product listing
    public SupplierProduct(int supplierId, int productId, BigDecimal pricePerUnit,
                           BigDecimal availableQuantity, BigDecimal minimumOrderQuantity,
                           String unitType, String qualityGrade, Date productionOrHarvestDate,
                           Date expiryDate) {
        this.supplierId = supplierId;
        this.productId = productId;
        this.pricePerUnit = pricePerUnit;
        this.availableQuantity = availableQuantity;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.unitType = unitType;
        this.qualityGrade = qualityGrade;
        this.productionOrHarvestDate = productionOrHarvestDate;
        this.expiryDate = expiryDate;
        this.listingStatus = "PENDING";
    }

    // Full Constructor
    public SupplierProduct(int supplierProductId, int supplierId, int productId,
                           BigDecimal pricePerUnit, BigDecimal availableQuantity,
                           BigDecimal minimumOrderQuantity, String unitType, String qualityGrade,
                           Date productionOrHarvestDate, Date expiryDate, String listingStatus,
                           Timestamp createdAt, Timestamp updatedAt) {
        this.supplierProductId = supplierProductId;
        this.supplierId = supplierId;
        this.productId = productId;
        this.pricePerUnit = pricePerUnit;
        this.availableQuantity = availableQuantity;
        this.minimumOrderQuantity = minimumOrderQuantity;
        this.unitType = unitType;
        this.qualityGrade = qualityGrade;
        this.productionOrHarvestDate = productionOrHarvestDate;
        this.expiryDate = expiryDate;
        this.listingStatus = listingStatus;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getSupplierProductId() { return supplierProductId; }
    public void setSupplierProductId(int supplierProductId) { this.supplierProductId = supplierProductId; }

    public int getSupplierId() { return supplierId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public BigDecimal getPricePerUnit() { return pricePerUnit; }
    public void setPricePerUnit(BigDecimal pricePerUnit) { this.pricePerUnit = pricePerUnit; }

    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }

    public BigDecimal getMinimumOrderQuantity() { return minimumOrderQuantity; }
    public void setMinimumOrderQuantity(BigDecimal minimumOrderQuantity) { this.minimumOrderQuantity = minimumOrderQuantity; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public String getQualityGrade() { return qualityGrade; }
    public void setQualityGrade(String qualityGrade) { this.qualityGrade = qualityGrade; }

    public Date getProductionOrHarvestDate() { return productionOrHarvestDate; }
    public void setProductionOrHarvestDate(Date productionOrHarvestDate) { this.productionOrHarvestDate = productionOrHarvestDate; }

    public Date getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Date expiryDate) { this.expiryDate = expiryDate; }

    public String getListingStatus() { return listingStatus; }
    public void setListingStatus(String listingStatus) { this.listingStatus = listingStatus; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}