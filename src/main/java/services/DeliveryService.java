package services;

import dao.deliveries;
import model.Delivery;

import java.sql.SQLException;
import java.util.List;

public class DeliveryService {

    private final deliveries deliveryDAO;

    public DeliveryService() {
        this.deliveryDAO = new deliveries();
    }

    public DeliveryService(deliveries deliveryDAO) {
        if (deliveryDAO == null) {
            throw new IllegalArgumentException(
                    "Delivery DAO cannot be null."
            );
        }

        this.deliveryDAO = deliveryDAO;
    }

    public boolean addDelivery(Delivery delivery)
            throws SQLException {

        validateDelivery(delivery);
        cleanDelivery(delivery);

        Delivery existingDelivery =
                deliveryDAO.getDeliveryByOrderId(
                        delivery.getOrderId()
                );

        if (existingDelivery != null) {
            throw new IllegalArgumentException(
                    "A delivery already exists for this order."
            );
        }

        delivery.setDeliveryStatus("PENDING");

        return deliveryDAO.addDelivery(delivery);
    }

    public Delivery getDeliveryById(int deliveryId)
            throws SQLException {

        validateDeliveryId(deliveryId);

        Delivery delivery =
                deliveryDAO.getDeliveryById(deliveryId);

        if (delivery == null) {
            throw new IllegalArgumentException(
                    "Delivery not found."
            );
        }

        return delivery;
    }

    public Delivery getDeliveryByOrderId(int orderId)
            throws SQLException {

        validateOrderId(orderId);

        Delivery delivery =
                deliveryDAO.getDeliveryByOrderId(orderId);

        if (delivery == null) {
            throw new IllegalArgumentException(
                    "Delivery not found for this order."
            );
        }

        return delivery;
    }

    public boolean updateLogisticsInfo(
            int deliveryId,
            String driverName,
            String driverPhone,
            String vehicleNumber
    ) throws SQLException {

        validateDeliveryId(deliveryId);

        Delivery delivery =
                deliveryDAO.getDeliveryById(deliveryId);

        if (delivery == null) {
            throw new IllegalArgumentException(
                    "Delivery not found."
            );
        }

        return deliveryDAO.updateLogisticsInfo(
                deliveryId,
                cleanOptionalValue(driverName),
                cleanOptionalValue(driverPhone),
                cleanOptionalValue(vehicleNumber)
        );
    }

    public boolean markAsDispatched(int deliveryId)
            throws SQLException {

        validateDeliveryId(deliveryId);

        Delivery delivery =
                deliveryDAO.getDeliveryById(deliveryId);

        if (delivery == null) {
            throw new IllegalArgumentException(
                    "Delivery not found."
            );
        }

        if ("DELIVERED".equalsIgnoreCase(
                delivery.getDeliveryStatus()
        )) {
            throw new IllegalArgumentException(
                    "A delivered order cannot be dispatched again."
            );
        }

        return deliveryDAO.markAsDispatched(deliveryId);
    }

    public boolean markAsDelivered(
            int deliveryId,
            String deliveryProof,
            String receivedBy
    ) throws SQLException {

        validateDeliveryId(deliveryId);

        Delivery delivery =
                deliveryDAO.getDeliveryById(deliveryId);

        if (delivery == null) {
            throw new IllegalArgumentException(
                    "Delivery not found."
            );
        }

        if ("DELIVERED".equalsIgnoreCase(
                delivery.getDeliveryStatus()
        )) {
            return true;
        }

        return deliveryDAO.markAsDelivered(
                deliveryId,
                cleanOptionalValue(deliveryProof),
                cleanOptionalValue(receivedBy)
        );
    }

    public boolean updateStatus(
            int deliveryId,
            String status
    ) throws SQLException {

        validateDeliveryId(deliveryId);

        String formattedStatus =
                validateAndFormatStatus(status);

        Delivery delivery =
                deliveryDAO.getDeliveryById(deliveryId);

        if (delivery == null) {
            throw new IllegalArgumentException(
                    "Delivery not found."
            );
        }

        if (formattedStatus.equalsIgnoreCase(
                delivery.getDeliveryStatus()
        )) {
            return true;
        }

        return deliveryDAO.updateStatus(
                deliveryId,
                formattedStatus
        );
    }

    public List<Delivery> getDeliveriesByStatus(
            String status
    ) throws SQLException {

        String formattedStatus =
                validateAndFormatStatus(status);

        return deliveryDAO.getDeliveriesByStatus(
                formattedStatus
        );
    }

    private void validateDelivery(Delivery delivery) {
        if (delivery == null) {
            throw new IllegalArgumentException(
                    "Delivery cannot be null."
            );
        }

        validateOrderId(delivery.getOrderId());

        if (isBlank(delivery.getDeliveryMethod())) {
            throw new IllegalArgumentException(
                    "Delivery method is required."
            );
        }
    }

    private void cleanDelivery(Delivery delivery) {
        delivery.setDeliveryMethod(
                delivery.getDeliveryMethod()
                        .trim()
                        .toUpperCase()
        );

        delivery.setDriverName(
                cleanOptionalValue(
                        delivery.getDriverName()
                )
        );

        delivery.setDriverPhone(
                cleanOptionalValue(
                        delivery.getDriverPhone()
                )
        );

        delivery.setVehicleNumber(
                cleanOptionalValue(
                        delivery.getVehicleNumber()
                )
        );

        delivery.setDeliveryProof(
                cleanOptionalValue(
                        delivery.getDeliveryProof()
                )
        );

        delivery.setReceivedBy(
                cleanOptionalValue(
                        delivery.getReceivedBy()
                )
        );
    }

    private String validateAndFormatStatus(String status) {
        if (isBlank(status)) {
            throw new IllegalArgumentException(
                    "Delivery status is required."
            );
        }

        String formattedStatus =
                status.trim().toUpperCase();

        if (!formattedStatus.equals("PENDING")
                && !formattedStatus.equals("DISPATCHED")
                && !formattedStatus.equals("IN_TRANSIT")
                && !formattedStatus.equals("DELIVERED")
                && !formattedStatus.equals("FAILED")) {

            throw new IllegalArgumentException(
                    "Invalid delivery status."
            );
        }

        return formattedStatus;
    }

    private void validateDeliveryId(int deliveryId) {
        if (deliveryId <= 0) {
            throw new IllegalArgumentException(
                    "Delivery ID must be greater than zero."
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