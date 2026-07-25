package service;

import dao.notifications;
import model.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {

    private final notifications notificationDAO;

    public NotificationService() {
        this.notificationDAO = new notifications();
    }

    public NotificationService(notifications notificationDAO) {
        if (notificationDAO == null) {
            throw new IllegalArgumentException(
                    "Notification DAO cannot be null."
            );
        }
        this.notificationDAO = notificationDAO;
    }

    public boolean addNotification(Notification notification)
            throws SQLException {

        validateNotification(notification);
        cleanNotification(notification);
        notification.setRead(false);

        return notificationDAO.addNotification(notification);
    }

    public Notification getNotificationById(int notificationId)
            throws SQLException {

        validateNotificationId(notificationId);

        Notification notification =
                notificationDAO.getNotificationById(notificationId);

        if (notification == null) {
            throw new IllegalArgumentException(
                    "Notification not found."
            );
        }

        return notification;
    }

    public List<Notification> getNotificationsByUserId(int userId)
            throws SQLException {

        validateUserId(userId);
        return notificationDAO.getNotificationsByUserId(userId);
    }

    public List<Notification> getUnreadNotificationsByUserId(
            int userId
    ) throws SQLException {

        validateUserId(userId);
        return notificationDAO
                .getUnreadNotificationsByUserId(userId);
    }

    public boolean markAsRead(int notificationId)
            throws SQLException {

        validateNotificationId(notificationId);

        Notification notification =
                notificationDAO.getNotificationById(notificationId);

        if (notification == null) {
            throw new IllegalArgumentException(
                    "Notification not found."
            );
        }

        if (notification.isRead()) {
            return true;
        }

        return notificationDAO.markAsRead(notificationId);
    }

    public boolean markAllAsReadForUser(int userId)
            throws SQLException {

        validateUserId(userId);
        return notificationDAO.markAllAsReadForUser(userId);
    }

    public int getUnreadCount(int userId)
            throws SQLException {

        validateUserId(userId);
        return notificationDAO.getUnreadCount(userId);
    }

    public boolean deleteNotification(int notificationId)
            throws SQLException {

        validateNotificationId(notificationId);

        Notification notification =
                notificationDAO.getNotificationById(notificationId);

        if (notification == null) {
            throw new IllegalArgumentException(
                    "Notification not found."
            );
        }

        return notificationDAO.deleteNotification(notificationId);
    }

    private void validateNotification(Notification notification) {
        if (notification == null) {
            throw new IllegalArgumentException(
                    "Notification cannot be null."
            );
        }

        validateUserId(notification.getUserId());

        if (isBlank(notification.getTitle())) {
            throw new IllegalArgumentException(
                    "Notification title is required."
            );
        }

        if (isBlank(notification.getMessage())) {
            throw new IllegalArgumentException(
                    "Notification message is required."
            );
        }

        if (isBlank(notification.getNotificationType())) {
            throw new IllegalArgumentException(
                    "Notification type is required."
            );
        }
    }

    private void cleanNotification(Notification notification) {
        notification.setTitle(
                notification.getTitle().trim()
        );

        notification.setMessage(
                notification.getMessage().trim()
        );

        notification.setNotificationType(
                notification.getNotificationType()
                        .trim()
                        .toUpperCase()
        );
    }

    private void validateNotificationId(int notificationId) {
        if (notificationId <= 0) {
            throw new IllegalArgumentException(
                    "Notification ID must be greater than zero."
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

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}