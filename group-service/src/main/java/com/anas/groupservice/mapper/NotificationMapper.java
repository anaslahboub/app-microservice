package com.anas.groupservice.mapper;

import com.anas.groupservice.dto.NotificationDTO;
import com.anas.groupservice.entity.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationDTO toDTO(Notification notification) {
        if (notification == null) {
            return null;
        }

        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        if (notification.getGroupId() != null) {
            dto.setGroupId(notification.getGroupId().toString());
        }
        dto.setGroupName(notification.getGroupName());
        dto.setMessage(notification.getMessage());
        dto.setUserId(notification.getUserId());
        dto.setUserName(notification.getUserName());
        if (notification.getCreatedDate() != null) {
            dto.setTimestamp(notification.getCreatedDate().toString());
        }
        dto.setRead(notification.isRead());
        if (notification.getRelatedEntityId() != null) {
            dto.setRelatedEntityId(notification.getRelatedEntityId());
        }
        dto.setRelatedEntityType(notification.getRelatedEntityType());
        
        return dto;
    }

    public Notification toEntity(NotificationDTO dto) {
        if (dto == null) {
            return null;
        }

        Notification notification = new Notification();
        notification.setId(dto.getId());
        notification.setType(dto.getType());
        if (dto.getGroupId() != null && !dto.getGroupId().isEmpty()) {
            notification.setGroupId(Long.valueOf(dto.getGroupId()));
        }
        notification.setGroupName(dto.getGroupName());
        notification.setMessage(dto.getMessage());
        notification.setUserId(dto.getUserId());
        notification.setUserName(dto.getUserName());
        notification.setRead(dto.isRead());
        if (dto.getRelatedEntityId() != null) {
            notification.setRelatedEntityId(dto.getRelatedEntityId());
        }
        notification.setRelatedEntityType(dto.getRelatedEntityType());
        
        return notification;
    }
}