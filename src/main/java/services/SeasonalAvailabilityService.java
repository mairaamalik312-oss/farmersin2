package services;

import dao.seasonal_availability;
import model.SeasonalAvailability;

import java.sql.SQLException;
import java.util.List;

public class SeasonalAvailabilityService {

    private final seasonal_availability availabilityDAO;

    public SeasonalAvailabilityService() {
        this.availabilityDAO = new seasonal_availability();
    }

    public SeasonalAvailabilityService(
            seasonal_availability availabilityDAO
    ) {
        if (availabilityDAO == null) {
            throw new IllegalArgumentException(
                    "Seasonal availability DAO cannot be null."
            );
        }
        this.availabilityDAO = availabilityDAO;
    }

    public boolean addSeasonalAvailability(
            SeasonalAvailability availability
    ) throws SQLException {

        validateAvailability(availability);
        cleanAvailability(availability);

        return availabilityDAO.addSeasonalAvailability(
                availability
        );
    }

    public SeasonalAvailability getAvailabilityById(
            int availabilityId
    ) throws SQLException {

        validateAvailabilityId(availabilityId);

        SeasonalAvailability availability =
                availabilityDAO.getAvailabilityById(
                        availabilityId
                );

        if (availability == null) {
            throw new IllegalArgumentException(
                    "Seasonal availability record not found."
            );
        }

        return availability;
    }

    public List<SeasonalAvailability> getAvailabilityByProductId(
            int productId
    ) throws SQLException {

        validateProductId(productId);

        return availabilityDAO.getAvailabilityByProductId(
                productId
        );
    }

    public List<SeasonalAvailability> getAvailableProductsByMonth(
            int month
    ) throws SQLException {

        validateMonth(month);

        return availabilityDAO.getAvailableProductsByMonth(
                month
        );
    }

    public boolean updateSeasonalAvailability(
            SeasonalAvailability availability
    ) throws SQLException {

        if (availability == null) {
            throw new IllegalArgumentException(
                    "Seasonal availability cannot be null."
            );
        }

        validateAvailabilityId(
                availability.getAvailabilityId()
        );

        validateAvailability(availability);
        cleanAvailability(availability);

        if (availabilityDAO.getAvailabilityById(
                availability.getAvailabilityId()
        ) == null) {
            throw new IllegalArgumentException(
                    "Seasonal availability record not found."
            );
        }

        return availabilityDAO.updateSeasonalAvailability(
                availability
        );
    }

    public boolean deleteSeasonalAvailability(
            int availabilityId
    ) throws SQLException {

        validateAvailabilityId(availabilityId);

        if (availabilityDAO.getAvailabilityById(
                availabilityId
        ) == null) {
            throw new IllegalArgumentException(
                    "Seasonal availability record not found."
            );
        }

        return availabilityDAO.deleteSeasonalAvailability(
                availabilityId
        );
    }

    private void validateAvailability(
            SeasonalAvailability availability
    ) {
        if (availability == null) {
            throw new IllegalArgumentException(
                    "Seasonal availability cannot be null."
            );
        }

        validateProductId(availability.getProductId());
        validateMonth(availability.getStartMonth());
        validateMonth(availability.getEndMonth());

        if (availability.getUpdatedBy() != null
                && availability.getUpdatedBy() <= 0) {
            throw new IllegalArgumentException(
                    "Updated-by user ID must be greater than zero."
            );
        }
    }

    private void cleanAvailability(
            SeasonalAvailability availability
    ) {
        if (availability.getRegion() != null) {
            String region = availability.getRegion().trim();

            availability.setRegion(
                    region.isEmpty() ? null : region
            );
        }
    }

    private void validateAvailabilityId(int availabilityId) {
        if (availabilityId <= 0) {
            throw new IllegalArgumentException(
                    "Availability ID must be greater than zero."
            );
        }
    }

    private void validateProductId(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be greater than zero."
            );
        }
    }

    private void validateMonth(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Month must be between 1 and 12."
            );
        }
    }
}