package model;

public class SeasonalAvailability {
    private int availabilityId;
    private int productId;
    private int startMonth; // 1-12
    private int endMonth;   // 1-12
    private String region;
    private boolean isCurrentlyAvailable;
    private Integer updatedBy; // Integer wrapper to allow NULL values

    // Default Constructor
    public SeasonalAvailability() {}

    // Constructor for creating a new seasonal entry
    public SeasonalAvailability(int productId, int startMonth, int endMonth,
                                String region, boolean isCurrentlyAvailable, Integer updatedBy) {
        this.productId = productId;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.region = region;
        this.isCurrentlyAvailable = isCurrentlyAvailable;
        this.updatedBy = updatedBy;
    }

    // Full Constructor
    public SeasonalAvailability(int availabilityId, int productId, int startMonth,
                                int endMonth, String region, boolean isCurrentlyAvailable,
                                Integer updatedBy) {
        this.availabilityId = availabilityId;
        this.productId = productId;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.region = region;
        this.isCurrentlyAvailable = isCurrentlyAvailable;
        this.updatedBy = updatedBy;
    }

    // Getters and Setters
    public int getAvailabilityId() { return availabilityId; }
    public void setAvailabilityId(int availabilityId) { this.availabilityId = availabilityId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getStartMonth() { return startMonth; }
    public void setStartMonth(int startMonth) { this.startMonth = startMonth; }

    public int getEndMonth() { return endMonth; }
    public void setEndMonth(int endMonth) { this.endMonth = endMonth; }

    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }

    public boolean isCurrentlyAvailable() { return isCurrentlyAvailable; }
    public void setCurrentlyAvailable(boolean currentlyAvailable) { isCurrentlyAvailable = currentlyAvailable; }

    public Integer getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Integer updatedBy) { this.updatedBy = updatedBy; }
}