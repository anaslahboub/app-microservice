package com.anas.chatservice.notification;

import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setChatId(notification.getChatId());
        dto.setContent(notification.getContent());
        dto.setSenderId(notification.getSenderId());
        dto.setReceiverId(notification.getReceiverId());
        dto.setChatName(notification.getChatName());
        dto.setMessageType(notification.getMessageType());
        dto.setType(notification.getType());
        dto.setMedia(notification.getMedia());
        dto.setRead(notification.isRead());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setRelatedEntityType(notification.getRelatedEntityType());
        
        return dto;
    }

    public Notification toEntity(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setChatId(dto.getChatId());
        notification.setContent(dto.getContent());
        notification.setSenderId(dto.getSenderId());
        notification.setReceiverId(dto.getReceiverId());
        notification.setChatName(dto.getChatName());
        notification.setMessageType(dto.getMessageType());
        notification.setType(dto.getType());
        notification.setMedia(dto.getMedia());
        notification.setRead(dto.isRead());
        notification.setRelatedEntityId(dto.getRelatedEntityId());
        notification.setRelatedEntityType(dto.getRelatedEntityType());
        
        return notification;
    }
}