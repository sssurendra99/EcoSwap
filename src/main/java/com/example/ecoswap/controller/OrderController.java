package com.example.ecoswap.controller;

import com.example.ecoswap.model.Order;
import com.example.ecoswap.model.User;
import com.example.ecoswap.model.enums.OrderStatus;
import com.example.ecoswap.model.enums.Role;
import com.example.ecoswap.security.CustomUserDetails;
import com.example.ecoswap.services.NotificationService;
import com.example.ecoswap.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/dashboard/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private NotificationService notificationService;

    /**
     * List all orders with pagination and filtering
     */
    @GetMapping
    public String listOrders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) OrderStatus status,
            Model model
    ) {
        User user = userDetails.getUser();
        Page<Order> orderPage;

        // Role-based order listing
        if (user.getRole() == Role.ADMIN) {
            // Admin sees all orders
            orderPage = orderService.getAllOrders(page, size);
        } else if (user.getRole() == Role.SELLER) {
            // Seller sees only their orders
            if (search != null && !search.isEmpty()) {
                orderPage = orderService.searchOrdersBySeller(user.getId(), search, page, size);
            } else if (status != null) {
                orderPage = orderService.getOrdersBySellerAndStatus(user.getId(), status, page, size);
            } else {
                orderPage = orderService.getOrdersBySeller(user.getId(), page, size);
            }
        } else {
            // Customers see their own orders
            orderPage = orderService.getOrdersByCustomer(user.getId(), page, size);
        }

        // Add orders and pagination to model
        model.addAttribute("orders", orderPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orderPage.getTotalPages());
        model.addAttribute("totalItems", orderPage.getTotalElements());

        // Add statistics for sellers
        if (user.getRole() == Role.SELLER) {
            Map<String, Object> stats = orderService.getSellerOrderStatistics(user.getId());
            model.addAttribute("stats", stats);
        }

        // Add order statuses for filter dropdown
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("searchQuery", search);

        // Add user info
        model.addAttribute("pageTitle", "Orders");
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());

        return "dashboard/orders_page";
    }

    /**
     * View order details
     */
    @GetMapping("/{id}")
    public String viewOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();
        Order order = orderService.getOrderById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));

        // Check if user has permission to view this order
        boolean hasAccess = false;
        if (user.getRole() == Role.ADMIN) {
            hasAccess = true;
        } else if (user.getRole() == Role.SELLER) {
            // Check if seller has items in this order
            hasAccess = order.getOrderItems().stream()
                .anyMatch(item -> item.getSeller().getId().equals(user.getId()));
        } else if (user.getRole() == Role.CUSTOMER) {
            hasAccess = order.getCustomer().getId().equals(user.getId());
        }

        if (!hasAccess) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to view this order");
            return "redirect:/dashboard/orders";
        }

        // Check if order status can be updated (not completed/cancelled)
        boolean canUpdateStatus = order.getStatus() != OrderStatus.CANCELLED
            && order.getStatus() != OrderStatus.DELIVERED;

        model.addAttribute("order", order);
        model.addAttribute("pageTitle", "Order #" + order.getOrderNumber());
        model.addAttribute("userName", user.getFullName());
        model.addAttribute("userRole", user.getRole().getDisplayName());
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("canUpdateStatus", canUpdateStatus);

        return "dashboard/order-details";
    }

    /**
     * Update order status
     */
    @PostMapping("/{id}/status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        try {
            // Only sellers and admins can update order status
            if (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
                redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to update order status");
                return "redirect:/dashboard/orders/" + id;
            }

            // Get the order
            Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

            // Additional check for sellers: verify they have items in this order
            if (user.getRole() == Role.SELLER) {
                boolean hasItemsInOrder = order.getOrderItems().stream()
                    .anyMatch(item -> item.getSeller().getId().equals(user.getId()));

                if (!hasItemsInOrder) {
                    redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to update this order");
                    return "redirect:/dashboard/orders";
                }
            }

            // Store old status for comparison
            OrderStatus oldStatus = order.getStatus();

            orderService.updateOrderStatus(id, status);

            // Create notification for customer based on the new status
            User customer = order.getCustomer();
            String notificationTitle = "";
            String notificationMessage = "";
            String notificationType = "INFO";
            String notificationIcon = "fas fa-info-circle";
            String notificationLink = "/dashboard/orders/" + id;

            switch (status) {
                case CONFIRMED:
                    notificationTitle = "Order Confirmed";
                    notificationMessage = "Your order #" + order.getOrderNumber() + " has been confirmed and is being prepared for shipment.";
                    notificationType = "SUCCESS";
                    notificationIcon = "fas fa-check-circle";
                    break;
                case PROCESSING:
                    notificationTitle = "Order Processing";
                    notificationMessage = "Your order #" + order.getOrderNumber() + " is now being processed.";
                    notificationType = "INFO";
                    notificationIcon = "fas fa-cog";
                    break;
                case SHIPPED:
                    notificationTitle = "Order Shipped";
                    notificationMessage = "Good news! Your order #" + order.getOrderNumber() + " has been shipped and is on its way.";
                    notificationType = "SUCCESS";
                    notificationIcon = "fas fa-shipping-fast";
                    break;
                case DELIVERED:
                    notificationTitle = "Order Delivered";
                    notificationMessage = "Your order #" + order.getOrderNumber() + " has been delivered. We hope you enjoy your sustainable purchase!";
                    notificationType = "SUCCESS";
                    notificationIcon = "fas fa-box-check";
                    break;
                case CANCELLED:
                    notificationTitle = "Order Cancelled";
                    notificationMessage = "Your order #" + order.getOrderNumber() + " has been cancelled.";
                    notificationType = "WARNING";
                    notificationIcon = "fas fa-times-circle";
                    break;
                case REFUNDED:
                    notificationTitle = "Order Refunded";
                    notificationMessage = "Your order #" + order.getOrderNumber() + " has been refunded. The amount will be credited to your account.";
                    notificationType = "INFO";
                    notificationIcon = "fas fa-undo";
                    break;
                default:
                    // Don't create notification for other statuses or if status hasn't changed
                    break;
            }

            // Only create notification if status actually changed and we have a message
            if (!status.equals(oldStatus) && !notificationMessage.isEmpty()) {
                notificationService.createNotification(
                    customer,
                    notificationTitle,
                    notificationMessage,
                    notificationType,
                    notificationIcon,
                    notificationLink
                );
            }

            redirectAttributes.addFlashAttribute("successMessage", "Order status updated to " + status.getDisplayName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating order status: " + e.getMessage());
        }

        return "redirect:/dashboard/orders/" + id;
    }

    /**
     * Update tracking number
     */
    @PostMapping("/{id}/tracking")
    public String updateTrackingNumber(
            @PathVariable Long id,
            @RequestParam String trackingNumber,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        User user = userDetails.getUser();

        try {
            if (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
                redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to update tracking number");
                return "redirect:/dashboard/orders/" + id;
            }

            // Get the order
            Order order = orderService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

            // Additional check for sellers: verify they have items in this order
            if (user.getRole() == Role.SELLER) {
                boolean hasItemsInOrder = order.getOrderItems().stream()
                    .anyMatch(item -> item.getSeller().getId().equals(user.getId()));

                if (!hasItemsInOrder) {
                    redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to update this order");
                    return "redirect:/dashboard/orders";
                }
            }

            orderService.updateTrackingNumber(id, trackingNumber);
            redirectAttributes.addFlashAttribute("successMessage", "Tracking number updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating tracking number: " + e.getMessage());
        }

        return "redirect:/dashboard/orders/" + id;
    }

    /**
     * Cancel order
     */
    @PostMapping("/{id}/cancel")
    public String cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.cancelOrder(id);
            redirectAttributes.addFlashAttribute("successMessage", "Order cancelled successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling order: " + e.getMessage());
        }

        return "redirect:/dashboard/orders/" + id;
    }

    /**
     * Delete order (admin only)
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User user = userDetails.getUser();

        if (user.getRole() != Role.ADMIN) {
            return "error: Unauthorized";
        }

        try {
            orderService.deleteOrder(id);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
}
