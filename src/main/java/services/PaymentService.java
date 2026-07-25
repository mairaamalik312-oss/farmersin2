package services;

import dao.payments;
import model.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PaymentService {

    private final payments paymentDAO;

    public PaymentService() {
        this.paymentDAO = new payments();
    }

    public PaymentService(payments paymentDAO) {
        if (paymentDAO == null) {
            throw new IllegalArgumentException(
                    "Payment DAO cannot be null."
            );
        }
        this.paymentDAO = paymentDAO;
    }

    public boolean addPayment(Payment payment)
            throws SQLException {

        validatePayment(payment);
        cleanPayment(payment);

        payment.setPaymentStatus("PENDING");

        return paymentDAO.addPayment(payment);
    }

    public Payment getPaymentById(int paymentId)
            throws SQLException {

        validatePaymentId(paymentId);

        Payment payment =
                paymentDAO.getPaymentById(paymentId);

        if (payment == null) {
            throw new IllegalArgumentException(
                    "Payment not found."
            );
        }

        return payment;
    }

    public List<Payment> getPaymentsByOrderId(int orderId)
            throws SQLException {

        validateOrderId(orderId);
        return paymentDAO.getPaymentsByOrderId(orderId);
    }

    public List<Payment> getPaymentsByBuyerId(int buyerId)
            throws SQLException {

        validateBuyerId(buyerId);
        return paymentDAO.getPaymentsByBuyerId(buyerId);
    }

    public boolean verifyPayment(
            int paymentId,
            int adminUserId,
            String status
    ) throws SQLException {

        validatePaymentId(paymentId);
        validateAdminUserId(adminUserId);

        String formattedStatus =
                validateAndFormatPaymentStatus(status);

        Payment payment =
                paymentDAO.getPaymentById(paymentId);

        if (payment == null) {
            throw new IllegalArgumentException(
                    "Payment not found."
            );
        }

        if (!formattedStatus.equals("SUCCESSFUL")
                && !formattedStatus.equals("FAILED")
                && !formattedStatus.equals("REFUNDED")) {

            throw new IllegalArgumentException(
                    "Verification status must be SUCCESSFUL, FAILED, or REFUNDED."
            );
        }

        return paymentDAO.verifyPayment(
                paymentId,
                adminUserId,
                formattedStatus
        );
    }

    private void validatePayment(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException(
                    "Payment cannot be null."
            );
        }

        validateOrderId(payment.getOrderId());
        validateBuyerId(payment.getBuyerId());

        if (isBlank(payment.getPaymentType())) {
            throw new IllegalArgumentException(
                    "Payment type is required."
            );
        }

        String paymentType =
                payment.getPaymentType()
                        .trim()
                        .toUpperCase();

        if (!paymentType.equals("ADVANCE")
                && !paymentType.equals("REMAINING")
                && !paymentType.equals("REFUND")) {

            throw new IllegalArgumentException(
                    "Invalid payment type."
            );
        }

        if (isBlank(payment.getPaymentMethod())) {
            throw new IllegalArgumentException(
                    "Payment method is required."
            );
        }

        if (payment.getAmount() == null) {
            throw new IllegalArgumentException(
                    "Payment amount is required."
            );
        }

        if (payment.getAmount()
                .compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero."
            );
        }
    }

    private void cleanPayment(Payment payment) {
        payment.setPaymentType(
                payment.getPaymentType()
                        .trim()
                        .toUpperCase()
        );

        payment.setPaymentMethod(
                payment.getPaymentMethod()
                        .trim()
                        .toUpperCase()
        );

        payment.setTransactionReference(
                cleanOptionalValue(
                        payment.getTransactionReference()
                )
        );

        payment.setProofImagePath(
                cleanOptionalValue(
                        payment.getProofImagePath()
                )
        );
    }

    private String validateAndFormatPaymentStatus(
            String status
    ) {
        if (isBlank(status)) {
            throw new IllegalArgumentException(
                    "Payment status is required."
            );
        }

        String formattedStatus =
                status.trim().toUpperCase();

        if (!formattedStatus.equals("PENDING")
                && !formattedStatus.equals("SUCCESSFUL")
                && !formattedStatus.equals("FAILED")
                && !formattedStatus.equals("REFUNDED")) {

            throw new IllegalArgumentException(
                    "Invalid payment status."
            );
        }

        return formattedStatus;
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

    private void validateBuyerId(int buyerId) {
        if (buyerId <= 0) {
            throw new IllegalArgumentException(
                    "Buyer ID must be greater than zero."
            );
        }
    }

    private void validateAdminUserId(int adminUserId) {
        if (adminUserId <= 0) {
            throw new IllegalArgumentException(
                    "Admin user ID must be greater than zero."
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
        return value == null || value.trim().isEmpty();
    }
}