package com.anas.groupservice.service;

import com.anas.groupservice.dto.NotificationDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

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