package com.example.ecoswap.services;

import com.example.ecoswap.model.Notification;
import com.example.ecoswap.model.User;
import com.example.ecoswap.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    /**
     * Create a new notification
     */
    @Transactional
    public Notification createNotification(User user, String title, String message, String type, String icon, String link) {
        Notification notification = new Notification(user, title, message, type, icon, link);
        return notificationRepository.save(notification);
    }

    /**
     * Create a success notification
     */
    @Transactional
    public Notification createSuccessNotification(User user, String title, String message, String link) {
        return createNotification(user, title, message, "SUCCESS", "fas fa-check-circle", link);
    }

    /**
     * Create an info notification
     */
    @Transactional
    public Notification createInfoNotification(User user, String title, String message, String link) {
        return createNotification(user, title, message, "INFO", "fas fa-info-circle", link);
    }

    /**
     * Create a warning notification
     */
    @Transactional
    public Notification createWarningNotification(User user, String title, String message, String link) {
        return createNotification(user, title, message, "WARNING", "fas fa-exclamation-triangle", link);
    }

    /**
     * Create an error notification
     */
    @Transactional
    public Notification createErrorNotification(User user, String title, String message, String link) {
        return createNotification(user, title, message, "ERROR", "fas fa-times-circle", link);
    }

    /**
     * Create order notification
     */
    @Transactional
    public Notification createOrderNotification(User user, String orderNumber, String status) {
        String title = "Order Update";
        String message = "Order #" + orderNumber + " - " + status;
        String link = "/dashboard/orders";
        return createNotification(user, title, message, "INFO", "fas fa-shopping-cart", link);
    }

    /**
     * Create product review notification
     */
    @Transactional
    public Notification createReviewNotification(User seller, String productName) {
        String title = "New Review";
        String message = "New review on " + productName;
        String link = "/seller/reviews";
        return createNotification(seller, title, message, "INFO", "fas fa-star", link);
    }

    /**
     * Create low stock notification
     */
    @Transactional
    public Notification createLowStockNotification(User seller, String productName, int stock) {
        String title = "Low Stock Alert";
        String message = productName + " - Only " + stock + " left in stock";
        String link = "/dashboard/products";
        return createNotification(seller, title, message, "WARNING", "fas fa-box", link);
    }

    /**
     * Get all notifications for a user
     */
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Get recent notifications (top 10)
     */
    public List<Notification> getRecentNotifications(User user) {
        return notificationRepository.findTop10ByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Get unread notifications
     */
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    /**
     * Count unread notifications
     */
    public Long countUnreadNotifications(User user) {
        Long count = notificationRepository.countByUserAndIsReadFalse(user);
        return count != null ? count : 0L;
    }

    /**
     * Count unread notifications by user ID
     */
    public Long countUnreadNotificationsByUserId(Long userId) {
        Long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return count != null ? count : 0L;
    }

    /**
     * Mark a notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = getUnreadNotifications(user);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        notificationRepository.saveAll(unreadNotifications);
    }

    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Delete all notifications for a user
     */
    @Transactional
    public void deleteAllUserNotifications(User user) {
        List<Notification> notifications = getUserNotifications(user);
        notificationRepository.deleteAll(notifications);
    }

    /**
     * Get notification by ID
     */
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }
}
