package service;

import dao.order_items;
import model.OrderItem;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class OrderItemService {

    private final order_items orderItemDAO;

    public OrderItemService() {
        this.orderItemDAO = new order_items();
    }

    public OrderItemService(order_items orderItemDAO) {
        if (orderItemDAO == null) {
            throw new IllegalArgumentException(
                    "Order item DAO cannot be null."
            );
        }
        this.orderItemDAO = orderItemDAO;
    }

    public boolean addOrderItem(OrderItem item)
            throws SQLException {

        validateOrderItem(item);
        prepareOrderItem(item);

        return orderItemDAO.addOrderItem(item);
    }

    public boolean addOrderItemsBatch(List<OrderItem> items)
            throws SQLException {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException(
                    "Order item list cannot be empty."
            );
        }

        for (OrderItem item : items) {
            validateOrderItem(item);
            prepareOrderItem(item);
        }

        return orderItemDAO.addOrderItemsBatch(items);
    }

    public OrderItem getOrderItemById(int orderItemId)
            throws SQLException {

        validateOrderItemId(orderItemId);

        OrderItem item =
                orderItemDAO.getOrderItemById(orderItemId);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Order item not found."
            );
        }

        return item;
    }

    public List<OrderItem> getItemsByOrderId(int orderId)
            throws SQLException {

        validateOrderId(orderId);
        return orderItemDAO.getItemsByOrderId(orderId);
    }

    public boolean updateOrderItem(OrderItem item)
            throws SQLException {

        if (item == null) {
            throw new IllegalArgumentException(
                    "Order item cannot be null."
            );
        }

        validateOrderItemId(item.getOrderItemId());
        validateOrderItem(item);
        prepareOrderItem(item);

        OrderItem existingItem =
                orderItemDAO.getOrderItemById(
                        item.getOrderItemId()
                );

        if (existingItem == null) {
            throw new IllegalArgumentException(
                    "Order item not found."
            );
        }

        return orderItemDAO.updateOrderItem(item);
    }

    public boolean deleteOrderItem(int orderItemId)
            throws SQLException {

        validateOrderItemId(orderItemId);

        OrderItem item =
                orderItemDAO.getOrderItemById(orderItemId);

        if (item == null) {
            throw new IllegalArgumentException(
                    "Order item not found."
            );
        }

        return orderItemDAO.deleteOrderItem(orderItemId);
    }

    public boolean deleteItemsByOrderId(int orderId)
            throws SQLException {

        validateOrderId(orderId);
        return orderItemDAO.deleteItemsByOrderId(orderId);
    }

    private void validateOrderItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "Order item cannot be null."
            );
        }

        validateOrderId(item.getOrderId());

        if (item.getSupplierProductId() <= 0) {
            throw new IllegalArgumentException(
                    "Supplier product ID must be greater than zero."
            );
        }

        validatePositiveAmount(
                item.getQuantity(),
                "Quantity"
        );

        validatePositiveAmount(
                item.getUnitPrice(),
                "Unit price"
        );
    }

    private void prepareOrderItem(OrderItem item) {
        item.setSubtotal(
                item.getQuantity().multiply(item.getUnitPrice())
        );

        if (item.getQualityGrade() != null) {
            String qualityGrade =
                    item.getQualityGrade().trim();

            item.setQualityGrade(
                    qualityGrade.isEmpty()
                            ? null
                            : qualityGrade.toUpperCase()
            );
        }
    }

    private void validatePositiveAmount(
            BigDecimal value,
            String fieldName
    ) {
        if (value == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    fieldName + " must be greater than zero."
            );
        }
    }

    private void validateOrderItemId(int orderItemId) {
        if (orderItemId <= 0) {
            throw new IllegalArgumentException(
                    "Order item ID must be greater than zero."
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
}