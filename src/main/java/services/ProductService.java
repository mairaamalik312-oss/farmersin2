package services;

import dao.products;
import model.Product;

import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final products productDAO;
    private Product product;

    public ProductService() {
        this.productDAO = new products();
    }

    public ProductService(products productDAO) {
        if (productDAO == null) {
            throw new IllegalArgumentException("Product DAO cannot be null.");
        }
        this.productDAO = productDAO;
    }

    public boolean addProduct(Product product) throws SQLException {
        validateProduct(product);
        cleanProduct(product);
        return productDAO.addProduct(product);
    }

    public Product getProductById(int productId) throws SQLException {
        validateProductId(productId);

        Product product = productDAO.getProductById(productId);

        if (product == null) {
            throw new IllegalArgumentException("Product not found.");
        }

        return product;
    }

    public List<Product> getAllActiveProducts() throws SQLException {
        return productDAO.getAllActiveProducts();
    }

    public List<Product> getProductsByCategoryId(int categoryId)
            throws SQLException {

        validateCategoryId(categoryId);
        return productDAO.getProductsByCategoryId(categoryId);
    }

    public boolean updateProduct(Product product)
            throws SQLException {

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }

        validateProductId(product.getProductId());
        validateProduct(product);
        cleanProduct(product);

        if (productDAO.getProductById(product.getProductId()) == null) {
            throw new IllegalArgumentException("Product not found.");
        }

        return productDAO.updateProduct(product);
    }

    public boolean setProductActiveStatus(
            int productId,
            boolean isActive
    ) throws SQLException {

        validateProductId(productId);

        if (productDAO.getProductById(productId) == null) {
            throw new IllegalArgumentException("Product not found.");
        }

        return productDAO.setProductActiveStatus(
                productId,
                isActive
        );
    }

    private void validateProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }

        if (isBlank(product.getProductName())) {
            throw new IllegalArgumentException("Product name is required.");
        }

        validateCategoryId(product.getCategoryId());

        if (product.getSubcategoryId() != null
                && product.getSubcategoryId() <= 0) {
            throw new IllegalArgumentException(
                    "Subcategory ID must be greater than zero."
            );
        }
    }

    private void cleanProduct(Product product) {
        product.setProductName(product.getProductName().trim());
        product.setDescription(cleanOptional(product.getDescription()));
        product.setImagePath(cleanOptional(product.getImagePath()));

        String unit = cleanOptional(product.getDefaultUnit());

        product.setDefaultUnit(
                unit == null ? null : unit.toUpperCase()
        );
        product.setSeason(cleanOptional(product.getSeason()));

        if (!product.isSeasonal()) {
            product.setSeason("All Year");
        }
    }

    private void validateProductId(int productId)
    {
        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be greater than zero."
            );

        }
        if (product.isSeasonal() && isBlank(product.getSeason())) {
            throw new IllegalArgumentException(
                    "Season is required for a seasonal product."
            );
        }

    }

    private void validateCategoryId(int categoryId) {
        if (categoryId <= 0) {
            throw new IllegalArgumentException(
                    "Category ID must be greater than zero."
            );
        }
    }

    private String cleanOptional(String value) {
        if (value == null) {
            return null;
        }

        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}