package com.example.ecoswap.services;

import com.example.ecoswap.model.Order;
import com.example.ecoswap.model.OrderItem;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.from-name}")
    private String fromName;

    /**
     * Send order confirmation email with receipt
     */
    public void sendOrderConfirmation(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("Order Confirmation - Order #" + order.getOrderNumber());

            String htmlContent = buildOrderConfirmationEmail(order);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Order confirmation email sent successfully to: " + order.getCustomerEmail());

        } catch (Exception e) {
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Build HTML email content for order confirmation
     */
    private String buildOrderConfirmationEmail(Order order) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        String orderDate = order.getCreatedAt().format(formatter);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset='UTF-8'>");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; background-color: #f4f4f4; margin: 0; padding: 0; }");
        html.append(".email-container { max-width: 600px; margin: 20px auto; background: white; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }");
        html.append(".header { background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); color: white; padding: 30px; text-align: center; }");
        html.append(".header h1 { margin: 0; font-size: 28px; }");
        html.append(".header p { margin: 10px 0 0 0; opacity: 0.9; }");
        html.append(".content { padding: 30px; }");
        html.append(".order-info { background: #f9fafb; padding: 20px; border-radius: 8px; margin-bottom: 20px; }");
        html.append(".info-row { display: flex; justify-content: space-between; margin-bottom: 10px; padding-bottom: 10px; border-bottom: 1px solid #e5e7eb; }");
        html.append(".info-row:last-child { border-bottom: none; margin-bottom: 0; }");
        html.append(".info-label { font-weight: 600; color: #6b7280; }");
        html.append(".info-value { color: #1f2937; }");
        html.append(".section-title { font-size: 18px; font-weight: 600; margin: 25px 0 15px 0; color: #1f2937; }");
        html.append(".items-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }");
        html.append(".items-table th { background: #f9fafb; padding: 12px; text-align: left; font-weight: 600; color: #374151; border-bottom: 2px solid #e5e7eb; }");
        html.append(".items-table td { padding: 12px; border-bottom: 1px solid #e5e7eb; }");
        html.append(".total-section { background: #f0fdf4; padding: 20px; border-radius: 8px; margin-top: 20px; }");
        html.append(".total-row { display: flex; justify-content: space-between; margin-bottom: 8px; }");
        html.append(".total-row.grand-total { font-size: 20px; font-weight: 700; color: #065f46; margin-top: 12px; padding-top: 12px; border-top: 2px solid #10b981; }");
        html.append(".shipping-info { background: #dbeafe; padding: 15px; border-radius: 8px; margin-top: 20px; }");
        html.append(".shipping-info h3 { margin-top: 0; color: #1e40af; font-size: 16px; }");
        html.append(".footer { background: #f9fafb; padding: 20px; text-align: center; color: #6b7280; font-size: 14px; }");
        html.append(".button { display: inline-block; padding: 12px 30px; background: #11998e; color: white; text-decoration: none; border-radius: 6px; margin: 20px 0; }");
        html.append(".eco-badge { display: inline-block; background: #d1fae5; color: #065f46; padding: 8px 16px; border-radius: 20px; font-size: 14px; margin: 10px 0; }");
        html.append("</style>");
        html.append("</head>");
        html.append("<body>");
        html.append("<div class='email-container'>");

        // Header
        html.append("<div class='header'>");
        html.append("<h1>🌱 Order Confirmed!</h1>");
        html.append("<p>Thank you for your purchase from EcoSwap</p>");
        html.append("</div>");

        // Content
        html.append("<div class='content'>");

        // Greeting
        html.append("<p>Hi ").append(order.getCustomer().getFullName()).append(",</p>");
        html.append("<p>Thank you for choosing EcoSwap! Your order has been received and is being processed.</p>");

        // Eco Badge
        html.append("<div class='eco-badge'>🌍 Eco-Friendly Order</div>");

        // Order Information
        html.append("<div class='order-info'>");
        html.append("<div class='info-row'>");
        html.append("<span class='info-label'>Order Number:</span>");
        html.append("<span class='info-value'><strong>").append(order.getOrderNumber()).append("</strong></span>");
        html.append("</div>");
        html.append("<div class='info-row'>");
        html.append("<span class='info-label'>Order Date:</span>");
        html.append("<span class='info-value'>").append(orderDate).append("</span>");
        html.append("</div>");
        html.append("<div class='info-row'>");
        html.append("<span class='info-label'>Payment Method:</span>");
        html.append("<span class='info-value'>").append(order.getPaymentMethod()).append("</span>");
        html.append("</div>");
        html.append("<div class='info-row'>");
        html.append("<span class='info-label'>Order Status:</span>");
        html.append("<span class='info-value'>").append(order.getStatus().name()).append("</span>");
        html.append("</div>");
        html.append("</div>");

        // Order Items
        html.append("<h2 class='section-title'>Order Items</h2>");
        html.append("<table class='items-table'>");
        html.append("<thead>");
        html.append("<tr>");
        html.append("<th>Product</th>");
        html.append("<th style='text-align: center;'>Qty</th>");
        html.append("<th style='text-align: right;'>Price</th>");
        html.append("<th style='text-align: right;'>Subtotal</th>");
        html.append("</tr>");
        html.append("</thead>");
        html.append("<tbody>");

        for (OrderItem item : order.getOrderItems()) {
            html.append("<tr>");
            html.append("<td>").append(item.getProductName()).append("</td>");
            html.append("<td style='text-align: center;'>").append(item.getQuantity()).append("</td>");
            html.append("<td style='text-align: right;'>$").append(String.format("%.2f", item.getPrice())).append("</td>");
            html.append("<td style='text-align: right;'>$").append(String.format("%.2f", item.getLineTotal())).append("</td>");
            html.append("</tr>");
        }

        html.append("</tbody>");
        html.append("</table>");

        // Total Section
        html.append("<div class='total-section'>");
        html.append("<div class='total-row'>");
        html.append("<span>Subtotal:</span>");
        html.append("<span>$").append(String.format("%.2f", order.getSubtotal())).append("</span>");
        html.append("</div>");
        html.append("<div class='total-row'>");
        html.append("<span>Shipping:</span>");
        html.append("<span>$").append(String.format("%.2f", order.getShippingCost())).append("</span>");
        html.append("</div>");
        html.append("<div class='total-row'>");
        html.append("<span>Tax:</span>");
        html.append("<span>$").append(String.format("%.2f", order.getTax())).append("</span>");
        html.append("</div>");
        html.append("<div class='total-row grand-total'>");
        html.append("<span>Total:</span>");
        html.append("<span>$").append(String.format("%.2f", order.getTotalAmount())).append("</span>");
        html.append("</div>");
        html.append("</div>");

        // Shipping Address
        html.append("<div class='shipping-info'>");
        html.append("<h3>📦 Shipping Address</h3>");
        html.append("<p style='margin: 5px 0;'><strong>").append(order.getCustomer().getFullName()).append("</strong></p>");
        html.append("<p style='margin: 5px 0;'>").append(order.getShippingAddress()).append("</p>");
        html.append("<p style='margin: 5px 0;'>").append(order.getShippingCity()).append(", ")
                .append(order.getShippingState()).append(" ").append(order.getShippingZipCode()).append("</p>");
        html.append("<p style='margin: 5px 0;'>").append(order.getShippingCountry()).append("</p>");
        html.append("<p style='margin: 5px 0;'>Phone: ").append(order.getCustomerPhone()).append("</p>");
        html.append("</div>");

        // Order Notes
        if (order.getOrderNotes() != null && !order.getOrderNotes().isEmpty()) {
            html.append("<h2 class='section-title'>Order Notes</h2>");
            html.append("<p style='background: #f9fafb; padding: 12px; border-radius: 6px;'>")
                    .append(order.getOrderNotes()).append("</p>");
        }

        // Closing
        html.append("<p style='margin-top: 30px;'>We'll send you a shipping confirmation email as soon as your items are on the way.</p>");
        html.append("<p>If you have any questions, please don't hesitate to contact us.</p>");
        html.append("<p>Thank you for supporting sustainable shopping! 🌍</p>");
        html.append("<p style='margin-top: 20px;'>Best regards,<br><strong>The EcoSwap Team</strong></p>");

        html.append("</div>");

        // Footer
        html.append("<div class='footer'>");
        html.append("<p>This is an automated email. Please do not reply to this message.</p>");
        html.append("<p>&copy; 2024 EcoSwap. All rights reserved.</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body>");
        html.append("</html>");

        return html.toString();
    }

    /**
     * Send order status update email
     */
    public void sendOrderStatusUpdate(Order order, String statusMessage) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(order.getCustomerEmail());
            helper.setSubject("Order Status Update - Order #" + order.getOrderNumber());

            String htmlContent = buildOrderStatusUpdateEmail(order, statusMessage);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Order status update email sent successfully to: " + order.getCustomerEmail());

        } catch (Exception e) {
            System.err.println("Failed to send order status update email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Build HTML email for order status update
     */
    private String buildOrderStatusUpdateEmail(Order order, String statusMessage) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'>");
        html.append("<style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background: #11998e; color: white; padding: 20px; text-align: center; }");
        html.append(".content { padding: 20px; background: white; }");
        html.append("</style></head><body>");
        html.append("<div class='container'>");
        html.append("<div class='header'><h1>Order Status Update</h1></div>");
        html.append("<div class='content'>");
        html.append("<p>Hi ").append(order.getCustomer().getFullName()).append(",</p>");
        html.append("<p>").append(statusMessage).append("</p>");
        html.append("<p><strong>Order Number:</strong> ").append(order.getOrderNumber()).append("</p>");
        html.append("<p><strong>Current Status:</strong> ").append(order.getStatus().name()).append("</p>");
        html.append("<p>Thank you for shopping with EcoSwap!</p>");
        html.append("</div></div></body></html>");
        return html.toString();
    }
}
