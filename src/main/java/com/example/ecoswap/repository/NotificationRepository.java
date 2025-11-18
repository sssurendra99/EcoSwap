package com.example.ecoswap.repository;

import com.example.ecoswap.model.Notification;
import com.example.ecoswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific user, ordered by creation date (newest first)
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find unread notifications for a specific user
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    /**
     * Count unread notifications for a specific user
     */
    Long countByUserAndIsReadFalse(User user);

    /**
     * Find recent notifications (limit in query)
     */
    List<Notification> findTop10ByUserOrderByCreatedAtDesc(User user);

    /**
     * Find by user ID
     */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Count unread by user ID
     */
    Long countByUserIdAndIsReadFalse(Long userId);
}
