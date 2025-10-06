package com.anas.chatservice.notification;

import com.anas.chatservice.common.BaseAuditingEntity;
import com.anas.chatservice.message.MessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification extends BaseAuditingEntity {

    @Id
    @SequenceGenerator(name = "notification_seq", sequenceName = "notification_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "notification_seq")
    private Long id;

    @Column(name = "chat_id")
    private String chatId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "sender_id")
    private String senderId;

    @Column(name = "receiver_id")
    private String receiverId;

    @Column(name = "chat_name")
    private String chatName;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type")
    private NotificationType type;

    @Column(name = "media", columnDefinition = "BYTEA")
    private byte[] media;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "related_entity_type")
    private String relatedEntityType;
}