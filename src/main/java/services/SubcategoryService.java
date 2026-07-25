package services;

import dao.subcategories;
import model.Subcategory;

import java.sql.SQLException;
import java.util.List;

public class SubcategoryService {

    private final subcategories subcategoryDAO;

    public SubcategoryService() {
        this.subcategoryDAO = new subcategories();
    }

    public SubcategoryService(subcategories subcategoryDAO) {
        if (subcategoryDAO == null) {
            throw new IllegalArgumentException(
                    "Subcategory DAO cannot be null."
            );
        }
        this.subcategoryDAO = subcategoryDAO;
    }

    public boolean addSubcategory(Subcategory subcategory)
            throws SQLException {

        validateSubcategory(subcategory);
        cleanSubcategory(subcategory);

        return subcategoryDAO.addSubcategory(subcategory);
    }

    public Subcategory getSubcategoryById(int subcategoryId)
            throws SQLException {

        validateSubcategoryId(subcategoryId);

        Subcategory subcategory =
                subcategoryDAO.getSubcategoryById(subcategoryId);

        if (subcategory == null) {
            throw new IllegalArgumentException(
                    "Subcategory not found."
            );
        }

        return subcategory;
    }

    public List<Subcategory> getSubcategoriesByCategoryId(
            int categoryId
    ) throws SQLException {

        validateCategoryId(categoryId);

        return subcategoryDAO.getSubcategoriesByCategoryId(
                categoryId
        );
    }

    public List<Subcategory> getAllActiveSubcategories()
            throws SQLException {

        return subcategoryDAO.getAllActiveSubcategories();
    }

    public boolean updateSubcategory(Subcategory subcategory)
            throws SQLException {

        if (subcategory == null) {
            throw new IllegalArgumentException(
                    "Subcategory cannot be null."
            );
        }

        validateSubcategoryId(
                subcategory.getSubcategoryId()
        );

        validateSubcategory(subcategory);
        cleanSubcategory(subcategory);

        if (subcategoryDAO.getSubcategoryById(
                subcategory.getSubcategoryId()
        ) == null) {
            throw new IllegalArgumentException(
                    "Subcategory not found."
            );
        }

        return subcategoryDAO.updateSubcategory(subcategory);
    }

    public boolean setSubcategoryActiveStatus(
            int subcategoryId,
            boolean isActive
    ) throws SQLException {

        validateSubcategoryId(subcategoryId);

        if (subcategoryDAO.getSubcategoryById(
                subcategoryId
        ) == null) {
            throw new IllegalArgumentException(
                    "Subcategory not found."
            );
        }

        return subcategoryDAO.setSubcategoryActiveStatus(
                subcategoryId,
                isActive
        );
    }

    private void validateSubcategory(
            Subcategory subcategory
    ) {
        if (subcategory == null) {
            throw new IllegalArgumentException(
                    "Subcategory cannot be null."
            );
        }

        validateCategoryId(subcategory.getCategoryId());

        if (isBlank(subcategory.getSubcategoryName())) {
            throw new IllegalArgumentException(
                    "Subcategory name is required."
            );
        }
    }

    private void cleanSubcategory(Subcategory subcategory) {
        subcategory.setSubcategoryName(
                subcategory.getSubcategoryName().trim()
        );
    }

    private void validateSubcategoryId(int subcategoryId) {
        if (subcategoryId <= 0) {
            throw new IllegalArgumentException(
                    "Subcategory ID must be greater than zero."
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
        return value == null || value.trim().isEmpty();
    }
}
