package model;

import java.sql.Timestamp;

public class AdminLog {
    private int logId;
    private int adminUserId;
    private String action;
    private String entityType;
    private Integer entityId; // Integer wrapper to allow NULL values
    private String details;   // Can be NULL
    private Timestamp actionDate;

    // Default Constructor
    public AdminLog() {}

    // Constructor for logging a new admin action
    public AdminLog(int adminUserId, String action, String entityType, Integer entityId, String details) {
        this.adminUserId = adminUserId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
    }

    // Full Constructor
    public AdminLog(int logId, int adminUserId, String action, String entityType,
                    Integer entityId, String details, Timestamp actionDate) {
        this.logId = logId;
        this.adminUserId = adminUserId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.details = details;
        this.actionDate = actionDate;
    }

    // Getters and Setters
    public int getLogId() { return logId; }
    public void setLogId(int logId) { this.logId = logId; }

    public int getAdminUserId() { return adminUserId; }
    public void setAdminUserId(int adminUserId) { this.adminUserId = adminUserId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }

    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Timestamp getActionDate() { return actionDate; }
    public void setActionDate(Timestamp actionDate) { this.actionDate = actionDate; }
}
