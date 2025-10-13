package com.anas.postservice.notification;

import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public Notification toEntity(NotificationDTO notificationDTO) {
        Notification notification = new Notification();
        notification.setId(notificationDTO.getId());
        notification.setType(notificationDTO.getType());
        notification.setPostId(notificationDTO.getPostId());
        notification.setPostContent(notificationDTO.getPostContent());
        notification.setMessage(notificationDTO.getMessage());
        notification.setUserId(notificationDTO.getUserId());
        notification.setUserName(notificationDTO.getUserName());
        notification.setRead(notificationDTO.isRead());
        notification.setRelatedEntityId(notificationDTO.getRelatedEntityId());
        notification.setRelatedEntityType(notificationDTO.getRelatedEntityType());
        return notification;
    }

    public NotificationDTO toDTO(Notification notification) {
        NotificationDTO notificationDTO = new NotificationDTO();
        notificationDTO.setId(notification.getId());
        notificationDTO.setType(notification.getType());
        notificationDTO.setPostId(notification.getPostId());
        notificationDTO.setPostContent(notification.getPostContent());
        notificationDTO.setMessage(notification.getMessage());
        notificationDTO.setUserId(notification.getUserId());
        notificationDTO.setUserName(notification.getUserName());
        notificationDTO.setRead(notification.isRead());
        notificationDTO.setRelatedEntityId(notification.getRelatedEntityId());
        notificationDTO.setRelatedEntityType(notification.getRelatedEntityType());
        notificationDTO.setCreatedDate(notification.getCreatedDate());
        notificationDTO.setLastModifiedDate(notification.getLastModifiedDate());
        return notificationDTO;
    }
}