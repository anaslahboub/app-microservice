package com.anas.postservice.entities;

import com.anas.postservice.common.BaseAuditingEntity;
import com.anas.postservice.enumeration.PostStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "posts")
public class Post extends BaseAuditingEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Id
    @SequenceGenerator(name = "post_id_seq", sequenceName = "post_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "post_id_seq")
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private PostStatus status = PostStatus.PENDING;

    // Store author ID directly instead of JPA relationship
    private String authorId;


    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> comments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Like> likes;
    
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Bookmark> bookmarks;

    private Long likeCount = 0L;

    private Long commentCount = 0L;
    
    private Long bookmarkCount = 0L;

    private boolean pinned = false;

    @Transient
    public boolean isApproved() {
        return PostStatus.APPROVED.equals(this.status);
    }

    @Transient
    public boolean isRejected() {
        return PostStatus.REJECTED.equals(this.status);
    }

    @Transient
    public boolean isPending() {
        return PostStatus.PENDING.equals(this.status);
    }
    
    @Transient
    public String getSummary() {
        if (this.content == null) {
            return "";
        }
        return this.content.length() > 100 ? this.content.substring(0, 100) + "..." : this.content;
    }
    @Transient
    public void incrementLikeCount() {
        this.likeCount++;
    }
    @Transient
    public void decrementLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }
}