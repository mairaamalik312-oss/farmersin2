package model;

public class Subcategory {
    private int subcategoryId;
    private int categoryId;
    private String subcategoryName;
    private boolean isActive;

    // Default Constructor
    public Subcategory() {}

    // Constructor for creating a new subcategory
    public Subcategory(int categoryId, String subcategoryName, boolean isActive) {
        this.categoryId = categoryId;
        this.subcategoryName = subcategoryName;
        this.isActive = isActive;
    }

    // Full Constructor
    public Subcategory(int subcategoryId, int categoryId, String subcategoryName, boolean isActive) {
        this.subcategoryId = subcategoryId;
        this.categoryId = categoryId;
        this.subcategoryName = subcategoryName;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getSubcategoryId() { return subcategoryId; }
    public void setSubcategoryId(int subcategoryId) { this.subcategoryId = subcategoryId; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public String getSubcategoryName() { return subcategoryName; }
    public void setSubcategoryName(String subcategoryName) { this.subcategoryName = subcategoryName; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}