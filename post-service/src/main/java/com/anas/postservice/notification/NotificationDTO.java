package com.anas.postservice.notification;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String type;
    private Long postId;
    private String postContent;
    private String message;
    private String userId;
    private String userName;
    private boolean isRead;
    private Long relatedEntityId;
    private String relatedEntityType;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}