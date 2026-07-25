package services;

import dao.refunds;
import model.Refund;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class RefundService {

    private final refunds refundDAO;

    public RefundService() {
        this.refundDAO = new refunds();
    }

    public RefundService(refunds refundDAO) {
        if (refundDAO == null) {
            throw new IllegalArgumentException("Refund DAO cannot be null.");
        }
        this.refundDAO = refundDAO;
    }

    public boolean addRefund(Refund refund)
            throws SQLException {

        validateRefund(refund);

        refund.setRefundReason(
                refund.getRefundReason().trim()
        );

        refund.setRefundStatus("REQUESTED");

        return refundDAO.addRefund(refund);
    }

    public Refund getRefundById(int refundId)
            throws SQLException {

        validateRefundId(refundId);

        Refund refund = refundDAO.getRefundById(refundId);

        if (refund == null) {
            throw new IllegalArgumentException("Refund not found.");
        }

        return refund;
    }

    public List<Refund> getRefundsByOrderId(int orderId)
            throws SQLException {

        validateOrderId(orderId);
        return refundDAO.getRefundsByOrderId(orderId);
    }

    public List<Refund> getRefundsByPaymentId(int paymentId)
            throws SQLException {

        validatePaymentId(paymentId);
        return refundDAO.getRefundsByPaymentId(paymentId);
    }

    public boolean updateRefundStatus(
            int refundId,
            String status
    ) throws SQLException {

        validateRefundId(refundId);

        String formattedStatus =
                validateAndFormatStatus(status);

        Refund refund = refundDAO.getRefundById(refundId);

        if (refund == null) {
            throw new IllegalArgumentException("Refund not found.");
        }

        if (formattedStatus.equalsIgnoreCase(
                refund.getRefundStatus()
        )) {
            return true;
        }

        return refundDAO.updateRefundStatus(
                refundId,
                formattedStatus
        );
    }

    public List<Refund> getPendingRefunds()
            throws SQLException {

        return refundDAO.getPendingRefunds();
    }

    private void validateRefund(Refund refund) {
        if (refund == null) {
            throw new IllegalArgumentException("Refund cannot be null.");
        }

        validatePaymentId(refund.getPaymentId());
        validateOrderId(refund.getOrderId());

        if (refund.getRefundAmount() == null) {
            throw new IllegalArgumentException(
                    "Refund amount is required."
            );
        }

        if (refund.getRefundAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Refund amount must be greater than zero."
            );
        }

        if (isBlank(refund.getRefundReason())) {
            throw new IllegalArgumentException(
                    "Refund reason is required."
            );
        }
    }

    private String validateAndFormatStatus(String status) {
        if (isBlank(status)) {
            throw new IllegalArgumentException(
                    "Refund status is required."
            );
        }

        String formatted = status.trim().toUpperCase();

        if (!formatted.equals("REQUESTED")
                && !formatted.equals("APPROVED")
                && !formatted.equals("REJECTED")
                && !formatted.equals("COMPLETED")) {
            throw new IllegalArgumentException(
                    "Invalid refund status."
            );
        }

        return formatted;
    }

    private void validateRefundId(int refundId) {
        if (refundId <= 0) {
            throw new IllegalArgumentException(
                    "Refund ID must be greater than zero."
            );
        }
    }

    private void validatePaymentId(int paymentId) {
        if (paymentId <= 0) {
            throw new IllegalArgumentException(
                    "Payment ID must be greater than zero."
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}