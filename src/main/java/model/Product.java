package model;

public class Product {
    private int productId;
    private String productName;
    private int categoryId;
    private Integer subcategoryId; // Integer wrapper to allow NULL values
    private String description;
    private String defaultUnit;
    private String imagePath;
    private boolean isSeasonal;
    private boolean isActive;

    // Default Constructor
    public Product() {}

    // Constructor for creating a new product
    public Product(String productName, int categoryId, Integer subcategoryId,
                   String description, String defaultUnit, String imagePath,
                   boolean isSeasonal, boolean isActive) {
        this.productName = productName;
        this.categoryId = categoryId;
        this.subcategoryId = subcategoryId;
        this.description = description;
        this.defaultUnit = defaultUnit;
        this.imagePath = imagePath;
        this.isSeasonal = isSeasonal;
        this.isActive = isActive;
    }

    // Full Constructor
    public Product(int productId, String productName, int categoryId, Integer subcategoryId,
                   String description, String defaultUnit, String imagePath,
                   boolean isSeasonal, boolean isActive) {
        this.productId = productId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.subcategoryId = subcategoryId;
        this.description = description;
        this.defaultUnit = defaultUnit;
        this.imagePath = imagePath;
        this.isSeasonal = isSeasonal;
        this.isActive = isActive;
    }

    // Getters and Setters
    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public Integer getSubcategoryId() { return subcategoryId; }
    public void setSubcategoryId(Integer subcategoryId) { this.subcategoryId = subcategoryId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDefaultUnit() { return defaultUnit; }
    public void setDefaultUnit(String defaultUnit) { this.defaultUnit = defaultUnit; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public boolean isSeasonal() { return isSeasonal; }
    public void setSeasonal(boolean seasonal) { isSeasonal = seasonal; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}