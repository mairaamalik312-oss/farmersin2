package services;

import dao.categories;
import model.Category;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {

    private final categories categoryDAO;

    public CategoryService() {
        this.categoryDAO = new categories();
    }

    public CategoryService(categories categoryDAO) {
        if (categoryDAO == null) {
            throw new IllegalArgumentException(
                    "Category DAO cannot be null."
            );
        }

        this.categoryDAO = categoryDAO;
    }

    public boolean addCategory(Category category)
            throws SQLException {

        validateCategory(category);
        cleanCategory(category);

        Category existingCategory =
                categoryDAO.getCategoryByName(
                        category.getCategoryName()
                );

        if (existingCategory != null) {
            throw new IllegalArgumentException(
                    "A category with this name already exists."
            );
        }

        return categoryDAO.addCategory(category);
    }

    public Category getCategoryById(int categoryId)
            throws SQLException {

        validateCategoryId(categoryId);

        Category category =
                categoryDAO.getCategoryById(categoryId);

        if (category == null) {
            throw new IllegalArgumentException(
                    "Category not found."
            );
        }

        return category;
    }

    public Category getCategoryByName(String categoryName)
            throws SQLException {

        if (isBlank(categoryName)) {
            throw new IllegalArgumentException(
                    "Category name is required."
            );
        }

        Category category =
                categoryDAO.getCategoryByName(
                        categoryName.trim()
                );

        if (category == null) {
            throw new IllegalArgumentException(
                    "Category not found."
            );
        }

        return category;
    }

    public List<Category> getActiveCategories()
            throws SQLException {

        return categoryDAO.getActiveCategories();
    }

    public List<Category> getAllCategories()
            throws SQLException {

        return categoryDAO.getAllCategories();
    }

    public boolean updateCategory(Category category)
            throws SQLException {

        if (category == null) {
            throw new IllegalArgumentException(
                    "Category cannot be null."
            );
        }

        validateCategoryId(category.getCategoryId());
        validateCategory(category);
        cleanCategory(category);

        Category existingCategory =
                categoryDAO.getCategoryById(
                        category.getCategoryId()
                );

        if (existingCategory == null) {
            throw new IllegalArgumentException(
                    "Category not found."
            );
        }

        Category categoryWithSameName =
                categoryDAO.getCategoryByName(
                        category.getCategoryName()
                );

        if (categoryWithSameName != null &&
                categoryWithSameName.getCategoryId()
                        != category.getCategoryId()) {

            throw new IllegalArgumentException(
                    "Another category with this name already exists."
            );
        }

        return categoryDAO.updateCategory(category);
    }

    public boolean setCategoryActiveStatus(
            int categoryId,
            boolean isActive
    ) throws SQLException {

        validateCategoryId(categoryId);

        Category category =
                categoryDAO.getCategoryById(categoryId);

        if (category == null) {
            throw new IllegalArgumentException(
                    "Category not found."
            );
        }

        if (category.isActive() == isActive) {
            return true;
        }

        return categoryDAO.setCategoryActiveStatus(
                categoryId,
                isActive
        );
    }

    public boolean deleteCategory(int categoryId)
            throws SQLException {

        validateCategoryId(categoryId);

        Category category =
                categoryDAO.getCategoryById(categoryId);

        if (category == null) {
            throw new IllegalArgumentException(
                    "Category not found."
            );
        }

        return categoryDAO.deleteCategory(categoryId);
    }

    private void validateCategory(Category category) {
        if (category == null) {
            throw new IllegalArgumentException(
                    "Category cannot be null."
            );
        }

        if (isBlank(category.getCategoryName())) {
            throw new IllegalArgumentException(
                    "Category name is required."
            );
        }
    }

    private void cleanCategory(Category category) {
        category.setCategoryName(
                category.getCategoryName().trim()
        );

        if (category.getDescription() != null) {
            String description =
                    category.getDescription().trim();

            category.setDescription(
                    description.isEmpty()
                            ? null
                            : description
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

    private boolean isBlank(String value) {
        return value == null ||
                value.trim().isEmpty();
    }
}
