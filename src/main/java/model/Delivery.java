package model;

import java.sql.Timestamp;

public class Delivery {
    private int deliveryId;
    private int orderId;
    private String deliveryMethod;
    private String driverName;     // Can be NULL
    private String driverPhone;    // Can be NULL
    private String vehicleNumber;  // Can be NULL
    private Timestamp dispatchDate; // Can be NULL
    private Timestamp deliveryDate; // Can be NULL
    private String deliveryStatus;  // PENDING, DISPATCHED, IN_TRANSIT, DELIVERED, FAILED
    private String deliveryProof;  // Can be NULL
    private String receivedBy;     // Can be NULL

    // Default Constructor
    public Delivery() {}

    // Constructor for initial delivery creation
    public Delivery(int orderId, String deliveryMethod) {
        this.orderId = orderId;
        this.deliveryMethod = deliveryMethod;
        this.deliveryStatus = "PENDING";
    }

    // Full Constructor
    public Delivery(int deliveryId, int orderId, String deliveryMethod, String driverName,
                    String driverPhone, String vehicleNumber, Timestamp dispatchDate,
                    Timestamp deliveryDate, String deliveryStatus, String deliveryProof,
                    String receivedBy) {
        this.deliveryId = deliveryId;
        this.orderId = orderId;
        this.deliveryMethod = deliveryMethod;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.vehicleNumber = vehicleNumber;
        this.dispatchDate = dispatchDate;
        this.deliveryDate = deliveryDate;
        this.deliveryStatus = deliveryStatus;
        this.deliveryProof = deliveryProof;
        this.receivedBy = receivedBy;
    }

    // Getters and Setters
    public int getDeliveryId() { return deliveryId; }
    public void setDeliveryId(int deliveryId) { this.deliveryId = deliveryId; }

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(String deliveryMethod) { this.deliveryMethod = deliveryMethod; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getDriverPhone() { return driverPhone; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public Timestamp getDispatchDate() { return dispatchDate; }
    public void setDispatchDate(Timestamp dispatchDate) { this.dispatchDate = dispatchDate; }

    public Timestamp getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(Timestamp deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getDeliveryStatus() { return deliveryStatus; }
    public void setDeliveryStatus(String deliveryStatus) { this.deliveryStatus = deliveryStatus; }

    public String getDeliveryProof() { return deliveryProof; }
    public void setDeliveryProof(String deliveryProof) { this.deliveryProof = deliveryProof; }

    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String receivedBy) { this.receivedBy = receivedBy; }
}