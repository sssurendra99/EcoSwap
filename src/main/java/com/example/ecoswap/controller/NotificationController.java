package com.example.ecoswap.controller;

import com.example.ecoswap.model.Notification;
import com.example.ecoswap.model.User;
import com.example.ecoswap.repository.UserRepository;
import com.example.ecoswap.services.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get recent notifications (AJAX)
     */
    @GetMapping("/recent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecentNotifications(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        List<Notification> notifications = notificationService.getRecentNotifications(user);
        Long unreadCount = notificationService.countUnreadNotifications(user);

        Map<String, Object> response = new HashMap<>();
        response.put("notifications", notifications);
        response.put("unreadCount", unreadCount);

        return ResponseEntity.ok(response);
    }

    /**
     * Get unread notification count (AJAX)
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        Long unreadCount = notificationService.countUnreadNotifications(user);

        Map<String, Object> response = new HashMap<>();
        response.put("count", unreadCount);

        return ResponseEntity.ok(response);
    }

    /**
     * Mark a notification as read (AJAX)
     */
    @PostMapping("/{id}/read")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAsRead(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow();

            // Verify notification belongs to user
            Notification notification = notificationService.getNotificationById(id).orElseThrow();
            if (!notification.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
            }

            notificationService.markAsRead(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("unreadCount", notificationService.countUnreadNotifications(user));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Mark all notifications as read (AJAX)
     */
    @PostMapping("/read-all")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow();

            notificationService.markAllAsRead(user);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("unreadCount", 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Delete a notification (AJAX)
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteNotification(
            @PathVariable Long id,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow();

            // Verify notification belongs to user
            Notification notification = notificationService.getNotificationById(id).orElseThrow();
            if (!notification.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("success", false, "message", "Unauthorized"));
            }

            notificationService.deleteNotification(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
