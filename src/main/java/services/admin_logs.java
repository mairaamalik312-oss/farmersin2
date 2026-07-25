package services;

import model.AdminLog;

import java.sql.SQLException;
import java.util.List;

public class admin_logs {

    private final dao.admin_logs adminLogDAO;

    // Default constructor
    public admin_logs() {
        this.adminLogDAO = new dao.admin_logs();
    }

    // Constructor for testing
    public admin_logs(dao.admin_logs adminLogDAO) {
        if (adminLogDAO == null) {
            throw new IllegalArgumentException(
                    "AdminLog DAO cannot be null."
            );
        }

        this.adminLogDAO = adminLogDAO;
    }

    // 1. Add a new admin log
    public boolean addLog(AdminLog log) throws SQLException {
        validateLog(log);

        /*
         * Standardize the action and entity type before
         * storing them in the database.
         */
        log.setAction(
                log.getAction().trim().toUpperCase()
        );

        if (log.getEntityType() != null) {
            String entityType = log.getEntityType().trim();

            if (entityType.isEmpty()) {
                log.setEntityType(null);
            } else {
                log.setEntityType(
                        entityType.toUpperCase()
                );
            }
        }

        if (log.getDetails() != null) {
            String details = log.getDetails().trim();

            if (details.isEmpty()) {
                log.setDetails(null);
            } else {
                log.setDetails(details);
            }
        }

        return adminLogDAO.addLog(log);
    }

    // 2. Get a log by its ID
    public AdminLog getLogById(int logId) throws SQLException {
        validateLogId(logId);

        AdminLog log = adminLogDAO.getLogById(logId);

        if (log == null) {
            throw new IllegalArgumentException(
                    "Admin log with ID " + logId + " was not found."
            );
        }

        return log;
    }

    // 3. Get all logs created by a specific admin
    public List<AdminLog> getLogsByAdminUserId(int adminUserId)
            throws SQLException {

        validateAdminUserId(adminUserId);

        return adminLogDAO.getLogsByAdminUserId(adminUserId);
    }

    // 4. Get logs by entity type
    public List<AdminLog> getLogsByEntityType(String entityType)
            throws SQLException {

        String validatedEntityType =
                validateAndFormatEntityType(entityType);

        return adminLogDAO.getLogsByEntityType(
                validatedEntityType
        );
    }

    // 5. Get logs for a specific entity
    public List<AdminLog> getLogsByEntity(
            String entityType,
            int entityId
    ) throws SQLException {

        String validatedEntityType =
                validateAndFormatEntityType(entityType);

        validateEntityId(entityId);

        return adminLogDAO.getLogsByEntity(
                validatedEntityType,
                entityId
        );
    }

    // 6. Get all admin logs
    public List<AdminLog> getAllLogs() throws SQLException {
        return adminLogDAO.getAllLogs();
    }

    // Validate a complete AdminLog object
    private void validateLog(AdminLog log) {
        if (log == null) {
            throw new IllegalArgumentException(
                    "Admin log cannot be null."
            );
        }

        validateAdminUserId(log.getAdminUserId());

        if (isBlank(log.getAction())) {
            throw new IllegalArgumentException(
                    "Admin action is required."
            );
        }

        if (log.getAction().trim().length() > 120) {
            throw new IllegalArgumentException(
                    "Admin action cannot exceed 120 characters."
            );
        }

        /*
         * Entity type and entity ID are optional.
         * However, an entity ID should not be provided
         * without an entity type.
         */
        if (log.getEntityId() != null) {
            validateEntityId(log.getEntityId());

            if (isBlank(log.getEntityType())) {
                throw new IllegalArgumentException(
                        "Entity type is required when entity ID is provided."
                );
            }
        }

        if (log.getEntityType() != null &&
                log.getEntityType().trim().length() > 50) {

            throw new IllegalArgumentException(
                    "Entity type cannot exceed 50 characters."
            );
        }
    }

    // Validate and standardize entity type
    private String validateAndFormatEntityType(
            String entityType
    ) {
        if (isBlank(entityType)) {
            throw new IllegalArgumentException(
                    "Entity type is required."
            );
        }

        String formattedEntityType =
                entityType.trim().toUpperCase();

        if (formattedEntityType.length() > 50) {
            throw new IllegalArgumentException(
                    "Entity type cannot exceed 50 characters."
            );
        }

        return formattedEntityType;
    }

    // Validate log ID
    private void validateLogId(int logId) {
        if (logId <= 0) {
            throw new IllegalArgumentException(
                    "Log ID must be greater than zero."
            );
        }
    }

    // Validate admin user ID
    private void validateAdminUserId(int adminUserId) {
        if (adminUserId <= 0) {
            throw new IllegalArgumentException(
                    "Admin user ID must be greater than zero."
            );
        }
    }

    // Validate entity ID
    private void validateEntityId(int entityId) {
        if (entityId <= 0) {
            throw new IllegalArgumentException(
                    "Entity ID must be greater than zero."
            );
        }
    }

    // Check whether a String is null, empty, or spaces only
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}