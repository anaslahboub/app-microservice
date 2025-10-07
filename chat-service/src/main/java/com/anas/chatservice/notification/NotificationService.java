package com.anas.chatservice.notification;

import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public NotificationDTO createAndSendNotification(String userId, Notification notification) {
        // Persist notification
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send real-time notification
        log.info("Sending WS notification to {} with payload {}", userId, notification);
        messagingTemplate.convertAndSendToUser(
                userId,
                "/chat",
                notification
        );
        
        return notificationMapper.toDTO(savedNotification);
    }

    @Transactional
    public NotificationDTO sendNotification(String userId, Notification notification) {
        // Persist notification
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send real-time notification
        log.info("Sending WS notification to {} with payload {}", userId, notification);
        messagingTemplate.convertAndSendToUser(
                userId,
                "/chat",
                notification
        );
        
        return notificationMapper.toDTO(savedNotification);
    }

    public List<NotificationDTO> getUserNotifications(String userId) {
        return notificationRepository.findByReceiverIdOrderByCreatedDateDesc(userId)
                .stream()
                .map(notificationMapper::toDTO)
                .toList();
    }

    public List<NotificationDTO> getUnreadUserNotifications(String userId) {
        return notificationRepository.findByReceiverIdAndIsReadOrderByCreatedDateDesc(userId, false)
                .stream()
                .map(notificationMapper::toDTO)
                .toList();
    }

    public List<NotificationDTO> getChatNotifications(String chatId) {
        return notificationRepository.findByChatIdOrderByCreatedDateDesc(chatId)
                .stream()
                .map(notificationMapper::toDTO)
                .toList();
    }

    public Long getUnreadNotificationCount(String userId) {
        return notificationRepository.countUnreadByReceiverId(userId);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsReadByReceiverId(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void  deleteNotification(Long notificationId){
        if(!notificationRepository.existsById(notificationId)){

            throw new NotFoundException("Notification not found");
        }
        notificationRepository.deleteById(notificationId);
    }
}