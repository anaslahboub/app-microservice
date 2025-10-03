package com.anas.postservice.entities;

import com.anas.postservice.common.BaseAuditingEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookmarks", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"post_id", "user_id"})
})
public class Bookmark extends BaseAuditingEntity {
    @Id
    @SequenceGenerator(name = "bookmark_id_seq", sequenceName = "bookmark_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "bookmark_id_seq")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;
    
    // Store user ID directly instead of JPA relationship
    private String userId;
}