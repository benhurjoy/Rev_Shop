package com.revshop.service;

import com.revshop.entity.Notification;
import com.revshop.entity.User;
import com.revshop.exception.ResourceNotFoundException;
import com.revshop.repository.NotificationRepository;
import com.revshop.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class NotificationService {

    private static final Logger logger = LogManager.getLogger(NotificationService.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // Self-injection to ensure Spring proxy is used for internal calls,
    // so @Transactional on sendNotification is respected.
    @Autowired
    @Lazy
    private NotificationService self;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendNotification(String email, String title,
                                 String message, Notification.NotificationType type) {
        logger.info("SendNotification called for: {} type: {}", email, type);
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

            Notification notification = Notification.builder()
                    .user(user)
                    .title(title)
                    .message(message)
                    .type(type)
                    .isRead(false)
                    .build();

            notificationRepository.save(notification);
            logger.info("Notification saved for: {} type: {}", email, type);
        } catch (Exception e) {
            logger.error("Failed to send notification to: {} - {}", email, e.getMessage());
        }
    }

    public List<Notification> getNotifications(String email) {
        logger.info("GetNotifications called for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return notificationRepository.findByUserWithDetails(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsRead(Long notificationId) {
        logger.info("MarkAsRead called for notificationId: {}", notificationId);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + notificationId));
        notification.setRead(true);
        notificationRepository.save(notification);
        logger.info("Notification marked as read: {}", notificationId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAllAsRead(String email) {
        logger.info("MarkAllAsRead called for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        notificationRepository.markAllAsReadByUser(user);
        logger.info("All notifications marked as read for: {}", email);
    }

    public long getUnreadCount(String email) {
        logger.info("GetUnreadCount called for: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Async
    public void sendOrderNotification(String email, String orderStatus, Long orderId) {
        logger.info("SendOrderNotification called for: {} orderId: {}", email, orderId);
        String title = "Order Update";
        String message;
        Notification.NotificationType type = Notification.NotificationType.ORDER_STATUS_UPDATED;

        switch (orderStatus) {
            case "PLACED":
                message = "Your order #" + orderId + " has been placed successfully!";
                type = Notification.NotificationType.ORDER_PLACED;
                break;
            case "PROCESSING":
                message = "Your order #" + orderId + " is now being processed.";
                break;
            case "SHIPPED":
                message = "Your order #" + orderId + " has been shipped!";
                break;
            case "DELIVERED":
                message = "Your order #" + orderId + " has been delivered. Enjoy!";
                break;
            case "CANCELLED":
                message = "Your order #" + orderId + " has been cancelled.";
                break;
            default:
                message = "Your order #" + orderId + " status updated to: " + orderStatus;
        }

        // Use self-proxy so @Transactional(REQUIRES_NEW) on sendNotification is applied
        self.sendNotification(email, title, message, type);
    }

    @Async
    public void sendLowStockAlert(String sellerEmail, String productName, int remaining) {
        logger.info("SendLowStockAlert called for seller: {} product: {}", sellerEmail, productName);
        String title = "Low Stock Alert";
        String message = "Your product \"" + productName
                + "\" is running low. Only " + remaining + " items left in stock!";
        // Use self-proxy so @Transactional(REQUIRES_NEW) on sendNotification is applied
        self.sendNotification(sellerEmail, title, message, Notification.NotificationType.LOW_STOCK);
    }
}
