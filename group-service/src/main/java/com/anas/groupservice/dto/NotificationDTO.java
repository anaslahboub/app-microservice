package com.anas.groupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO {
    private Long id;
    private String type;
    private String groupId;
    private String groupName;
    private String message;
    private String userId;
    private String userName;
    private String timestamp;
    private boolean isRead;
    private Long relatedEntityId;
    private String relatedEntityType;
}