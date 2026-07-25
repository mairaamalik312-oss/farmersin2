package services;

import dao.complaints;
import model.Complaint;

import java.sql.SQLException;
import java.util.List;

public class ComplaintService {

    private final complaints complaintDAO;

    public ComplaintService() {
        this.complaintDAO = new complaints();
    }

    public ComplaintService(complaints complaintDAO) {
        if (complaintDAO == null) {
            throw new IllegalArgumentException(
                    "Complaint DAO cannot be null."
            );
        }

        this.complaintDAO = complaintDAO;
    }

    public boolean addComplaint(Complaint complaint)
            throws SQLException {

        validateComplaint(complaint);
        cleanComplaint(complaint);

        complaint.setComplaintStatus("OPEN");

        return complaintDAO.addComplaint(complaint);
    }

    public Complaint getComplaintById(int complaintId)
            throws SQLException {

        validateComplaintId(complaintId);

        Complaint complaint =
                complaintDAO.getComplaintById(complaintId);

        if (complaint == null) {
            throw new IllegalArgumentException(
                    "Complaint not found."
            );
        }

        return complaint;
    }

    public List<Complaint> getComplaintsSubmittedBy(int userId)
            throws SQLException {

        validateUserId(userId);

        return complaintDAO.getComplaintsSubmittedBy(userId);
    }

    public List<Complaint> getComplaintsAgainstUser(int userId)
            throws SQLException {

        validateUserId(userId);

        return complaintDAO.getComplaintsAgainstUser(userId);
    }

    public List<Complaint> getComplaintsByOrderId(int orderId)
            throws SQLException {

        validateOrderId(orderId);

        return complaintDAO.getComplaintsByOrderId(orderId);
    }

    public List<Complaint> getComplaintsByStatus(String status)
            throws SQLException {

        String formattedStatus =
                validateAndFormatStatus(status);

        return complaintDAO.getComplaintsByStatus(
                formattedStatus
        );
    }

    public boolean resolveComplaint(
            int complaintId,
            String newStatus,
            String adminResponse
    ) throws SQLException {

        validateComplaintId(complaintId);

        String formattedStatus =
                validateResolutionStatus(newStatus);

        if (isBlank(adminResponse)) {
            throw new IllegalArgumentException(
                    "Admin response is required."
            );
        }

        Complaint complaint =
                complaintDAO.getComplaintById(complaintId);

        if (complaint == null) {
            throw new IllegalArgumentException(
                    "Complaint not found."
            );
        }

        return complaintDAO.resolveComplaint(
                complaintId,
                formattedStatus,
                adminResponse.trim()
        );
    }

    public boolean updateStatus(
            int complaintId,
            String newStatus
    ) throws SQLException {

        validateComplaintId(complaintId);

        String formattedStatus =
                validateAndFormatStatus(newStatus);

        Complaint complaint =
                complaintDAO.getComplaintById(complaintId);

        if (complaint == null) {
            throw new IllegalArgumentException(
                    "Complaint not found."
            );
        }

        if (formattedStatus.equalsIgnoreCase(
                complaint.getComplaintStatus()
        )) {
            return true;
        }

        return complaintDAO.updateStatus(
                complaintId,
                formattedStatus
        );
    }

    private void validateComplaint(Complaint complaint) {
        if (complaint == null) {
            throw new IllegalArgumentException(
                    "Complaint cannot be null."
            );
        }

        if (complaint.getOrderId() != null) {
            validateOrderId(complaint.getOrderId());
        }

        validateUserId(complaint.getSubmittedBy());
        validateUserId(complaint.getAgainstUserId());

        if (complaint.getSubmittedBy()
                == complaint.getAgainstUserId()) {

            throw new IllegalArgumentException(
                    "A user cannot submit a complaint against themselves."
            );
        }

        if (isBlank(complaint.getComplaintType())) {
            throw new IllegalArgumentException(
                    "Complaint type is required."
            );
        }

        if (isBlank(complaint.getDescription())) {
            throw new IllegalArgumentException(
                    "Complaint description is required."
            );
        }
    }

    private void cleanComplaint(Complaint complaint) {
        complaint.setComplaintType(
                complaint.getComplaintType()
                        .trim()
                        .toUpperCase()
        );

        complaint.setDescription(
                complaint.getDescription().trim()
        );

        complaint.setEvidencePath(
                cleanOptionalValue(
                        complaint.getEvidencePath()
                )
        );
    }

    private String validateAndFormatStatus(String status) {
        if (isBlank(status)) {
            throw new IllegalArgumentException(
                    "Complaint status is required."
            );
        }

        String formattedStatus =
                status.trim().toUpperCase();

        if (!formattedStatus.equals("OPEN")
                && !formattedStatus.equals("UNDER_REVIEW")
                && !formattedStatus.equals("RESOLVED")
                && !formattedStatus.equals("REJECTED")) {

            throw new IllegalArgumentException(
                    "Invalid complaint status."
            );
        }

        return formattedStatus;
    }

    private String validateResolutionStatus(String status) {
        String formattedStatus =
                validateAndFormatStatus(status);

        if (!formattedStatus.equals("RESOLVED")
                && !formattedStatus.equals("REJECTED")) {

            throw new IllegalArgumentException(
                    "Resolution status must be RESOLVED or REJECTED."
            );
        }

        return formattedStatus;
    }

    private void validateComplaintId(int complaintId) {
        if (complaintId <= 0) {
            throw new IllegalArgumentException(
                    "Complaint ID must be greater than zero."
            );
        }
    }

    private void validateUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be greater than zero."
            );
        }
    }

    private void validateOrderId(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Order ID must be greater than zero."
            );
        }
    }

    private String cleanOptionalValue(String value) {
        if (value == null) {
            return null;
        }

        String cleanedValue = value.trim();

        return cleanedValue.isEmpty()
                ? null
                : cleanedValue;
    }

    private boolean isBlank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}