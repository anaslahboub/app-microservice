package com.anas.postservice.notification;

import com.anas.postservice.common.BaseAuditingEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification extends BaseAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "post_content")
    private String postContent;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "is_read")
    private boolean isRead = false;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "related_entity_type")
    private String relatedEntityType;
}