package com.anas.groupservice.service;

import com.anas.groupservice.dto.NotificationDTO;
import com.anas.groupservice.entity.Notification;
import com.anas.groupservice.mapper.NotificationMapper;
import com.anas.groupservice.repository.NotificationRepository;
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
    public NotificationDTO createAndSendGroupNotification(String groupId, NotificationDTO notificationDTO) {
        // Persist notification
        Notification notification = notificationMapper.toEntity(notificationDTO);
        if (groupId != null && !groupId.isEmpty()) {
            notification.setGroupId(Long.valueOf(groupId));
        }
        notification.setRead(false);
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send real-time notification
        log.info("Sending notification to group {}: {}", groupId, notificationDTO);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, notificationDTO);
        
        return notificationMapper.toDTO(savedNotification);
    }

    @Transactional
    public NotificationDTO createAndSendUserNotification(String userId, NotificationDTO notificationDTO) {
        // Persist notification
        Notification notification = notificationMapper.toEntity(notificationDTO);
        notification.setUserId(userId);
        notification.setRead(false);
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send real-time notification
        log.info("Sending notification to user {}: {}", userId, notificationDTO);
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notificationDTO);
        
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

    public List<NotificationDTO> getGroupNotifications(Long groupId) {
        return notificationRepository.findByGroupIdOrderByCreatedDateDesc(groupId)
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

    public void sendGroupNotification(String groupId, NotificationDTO notification) {
        log.info("Sending notification to group {}: {}", groupId, notification);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, notification);
    }

    public void sendUserNotification(String userId, NotificationDTO notification) {
        log.info("Sending notification to user {}: {}", userId, notification);
        messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", notification);
    }

    public void sendNotificationToAll(NotificationDTO notification) {
        log.info("Sending broadcast notification: {}", notification);
        messagingTemplate.convertAndSend("/topic/notifications", notification);
    }
}