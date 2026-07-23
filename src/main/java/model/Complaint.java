package model;

import java.sql.Timestamp;

public class Complaint {
    private int complaintId;
    private Integer orderId; // Wrapper class to allow NULL
    private int submittedBy;
    private int againstUserId;
    private String complaintType;
    private String description;
    private String evidencePath; // Can be NULL
    private String complaintStatus; // OPEN, UNDER_REVIEW, RESOLVED, REJECTED
    private String adminResponse; // Can be NULL
    private Timestamp createdAt;
    private Timestamp resolvedAt; // Can be NULL

    // Default Constructor
    public Complaint() {}

    // Constructor for submitting a new complaint
    public Complaint(Integer orderId, int submittedBy, int againstUserId,
                     String complaintType, String description, String evidencePath) {
        this.orderId = orderId;
        this.submittedBy = submittedBy;
        this.againstUserId = againstUserId;
        this.complaintType = complaintType;
        this.description = description;
        this.evidencePath = evidencePath;
        this.complaintStatus = "OPEN";
    }

    // Full Constructor
    public Complaint(int complaintId, Integer orderId, int submittedBy, int againstUserId,
                     String complaintType, String description, String evidencePath,
                     String complaintStatus, String adminResponse,
                     Timestamp createdAt, Timestamp resolvedAt) {
        this.complaintId = complaintId;
        this.orderId = orderId;
        this.submittedBy = submittedBy;
        this.againstUserId = againstUserId;
        this.complaintType = complaintType;
        this.description = description;
        this.evidencePath = evidencePath;
        this.complaintStatus = complaintStatus;
        this.adminResponse = adminResponse;
        this.createdAt = createdAt;
        this.resolvedAt = resolvedAt;
    }

    // Getters and Setters
    public int getComplaintId() { return complaintId; }
    public void setComplaintId(int complaintId) { this.complaintId = complaintId; }

    public Integer getOrderId() { return orderId; }
    public void setOrderId(Integer orderId) { this.orderId = orderId; }

    public int getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(int submittedBy) { this.submittedBy = submittedBy; }

    public int getAgainstUserId() { return againstUserId; }
    public void setAgainstUserId(int againstUserId) { this.againstUserId = againstUserId; }

    public String getComplaintType() { return complaintType; }
    public void setComplaintType(String complaintType) { this.complaintType = complaintType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getEvidencePath() { return evidencePath; }
    public void setEvidencePath(String evidencePath) { this.evidencePath = evidencePath; }

    public String getComplaintStatus() { return complaintStatus; }
    public void setComplaintStatus(String complaintStatus) { this.complaintStatus = complaintStatus; }

    public String getAdminResponse() { return adminResponse; }
    public void setAdminResponse(String adminResponse) { this.adminResponse = adminResponse; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Timestamp resolvedAt) { this.resolvedAt = resolvedAt; }
}