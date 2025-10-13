package com.anas.postservice.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Transactional
    public NotificationDTO createAndSendPostNotification(NotificationDTO notificationDTO) {
        // Persist notification
        Notification notification = notificationMapper.toEntity(notificationDTO);
        notification.setRead(false);
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send real-time notification
        log.info("Sending post notification to user {}: {}", notificationDTO.getUserId(), notificationDTO);
        messagingTemplate.convertAndSendToUser(notificationDTO.getUserId(), "/queue/notifications", notificationDTO);
        
        return notificationMapper.toDTO(savedNotification);
    }

    @Transactional
    public NotificationDTO createAndSendBroadcastNotification(NotificationDTO notificationDTO) {
        // Persist notification
        Notification notification = notificationMapper.toEntity(notificationDTO);
        notification.setRead(false);
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send real-time notification
        log.info("Sending broadcast notification: {}", notificationDTO);
        messagingTemplate.convertAndSend("/topic/notifications", notificationDTO);
        
        return notificationMapper.toDTO(savedNotification);
    }

    public List<NotificationDTO> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedDateDesc(userId)
                .stream()
                .map(notificationMapper::toDTO)
                .toList();
    }

    public List<NotificationDTO> getUnreadUserNotifications(String userId) {
        return notificationRepository.findByUserIdAndIsReadOrderByCreatedDateDesc(userId, false)
                .stream()
                .map(notificationMapper::toDTO)
                .toList();
    }

    public List<NotificationDTO> getPostNotifications(Long postId) {
        return notificationRepository.findByPostIdOrderByCreatedDateDesc(postId)
                .stream()
                .map(notificationMapper::toDTO)
                .toList();
    }

    public Long getUnreadNotificationCount(String userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void sendUserNotification(String userId, NotificationDTO notification) {
        log.info("Sending notification to user {}: {}", userId, notification);
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }

    public void sendNotificationToAll(NotificationDTO notification) {
        log.info("Sending broadcast notification: {}", notification);
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
}