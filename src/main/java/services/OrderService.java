package services;

import dao.orders;
import model.Order;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class OrderService {

    private final orders orderDAO;

    public OrderService() {
        this.orderDAO = new orders();
    }

    public OrderService(orders orderDAO) {
        if (orderDAO == null) {
            throw new IllegalArgumentException(
                    "Order DAO cannot be null."
            );
        }
        this.orderDAO = orderDAO;
    }

    public boolean addOrder(Order order)
            throws SQLException {

        validateOrder(order);
        cleanOrder(order);

        order.calculateTotals();

        if (order.getTotalAmount()
                .compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Total amount cannot be negative."
            );
        }

        order.setOrderStatus("PENDING");
        order.setPaymentStatus("UNPAID");

        return orderDAO.addOrder(order);
    }

    public Order getOrderById(int orderId)
            throws SQLException {

        validateOrderId(orderId);

        Order order = orderDAO.getOrderById(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order not found."
            );
        }

        return order;
    }

    public List<Order> getOrdersByBuyerId(int buyerId)
            throws SQLException {

        validateBuyerId(buyerId);
        return orderDAO.getOrdersByBuyerId(buyerId);
    }

    public List<Order> getOrdersBySupplierId(int supplierId)
            throws SQLException {

        validateSupplierId(supplierId);
        return orderDAO.getOrdersBySupplierId(supplierId);
    }

    public boolean updateOrderStatus(
            int orderId,
            String status
    ) throws SQLException {

        validateOrderId(orderId);

        String formattedStatus =
                validateAndFormatOrderStatus(status);

        Order order = orderDAO.getOrderById(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order not found."
            );
        }

        if (formattedStatus.equalsIgnoreCase(
                order.getOrderStatus()
        )) {
            return true;
        }

        return orderDAO.updateOrderStatus(
                orderId,
                formattedStatus
        );
    }

    public boolean updatePaymentStatus(
            int orderId,
            String paymentStatus
    ) throws SQLException {

        validateOrderId(orderId);

        String formattedStatus =
                validateAndFormatPaymentStatus(
                        paymentStatus
                );

        Order order = orderDAO.getOrderById(orderId);

        if (order == null) {
            throw new IllegalArgumentException(
                    "Order not found."
            );
        }

        if (formattedStatus.equalsIgnoreCase(
                order.getPaymentStatus()
        )) {
            return true;
        }

        return orderDAO.updatePaymentStatus(
                orderId,
                formattedStatus
        );
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException(
                    "Order cannot be null."
            );
        }

        validateBuyerId(order.getBuyerId());
        validateSupplierId(order.getSupplierId());

        if (order.getDeliveryAddressId() <= 0) {
            throw new IllegalArgumentException(
                    "Delivery address ID must be greater than zero."
            );
        }

        validateNonNegativeAmount(
                order.getProductTotal(),
                "Product total"
        );

        validateNonNegativeAmount(
                order.getDeliveryCharge(),
                "Delivery charge"
        );

        validateNonNegativeAmount(
                order.getDiscountAmount(),
                "Discount amount"
        );

        validateNonNegativeAmount(
                order.getAdvancePercentage(),
                "Advance percentage"
        );

        if (order.getAdvancePercentage()
                .compareTo(new BigDecimal("100")) > 0) {

            throw new IllegalArgumentException(
                    "Advance percentage cannot exceed 100."
            );
        }
    }

    private void cleanOrder(Order order) {
        if (order.getNotes() != null) {
            String notes = order.getNotes().trim();

            order.setNotes(
                    notes.isEmpty() ? null : notes
            );
        }
    }

    private String validateAndFormatOrderStatus(
            String status
    ) {
        if (isBlank(status)) {
            throw new IllegalArgumentException(
                    "Order status is required."
            );
        }

        String formattedStatus =
                status.trim().toUpperCase();

        if (!formattedStatus.equals("PENDING")
                && !formattedStatus.equals("ACCEPTED")
                && !formattedStatus.equals("PROCESSING")
                && !formattedStatus.equals("DISPATCHED")
                && !formattedStatus.equals("DELIVERED")
                && !formattedStatus.equals("CANCELLED")
                && !formattedStatus.equals("REJECTED")) {

            throw new IllegalArgumentException(
                    "Invalid order status."
            );
        }

        return formattedStatus;
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

        if (!formattedStatus.equals("UNPAID")
                && !formattedStatus.equals("ADVANCE_PAID")
                && !formattedStatus.equals("PARTIALLY_PAID")
                && !formattedStatus.equals("FULLY_PAID")
                && !formattedStatus.equals("REFUNDED")) {

            throw new IllegalArgumentException(
                    "Invalid payment status."
            );
        }

        return formattedStatus;
    }

    private void validateNonNegativeAmount(
            BigDecimal value,
            String fieldName
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }

        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative."
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

    private void validateSupplierId(int supplierId) {
        if (supplierId <= 0) {
            throw new IllegalArgumentException(
                    "Supplier ID must be greater than zero."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}