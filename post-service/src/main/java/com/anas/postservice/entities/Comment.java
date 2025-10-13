package com.anas.postservice.entities;


import com.anas.postservice.common.BaseAuditingEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "comments")
public class Comment extends BaseAuditingEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    @Id
    @SequenceGenerator(name = "comment_id_seq", sequenceName = "comment_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "comment_id_seq")
    private Long id;
    
    @Column(columnDefinition = "TEXT")
    private String content;
    
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;
    
    // Store author ID directly instead of JPA relationship
    private String authorId;
    
    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    @JsonIgnore
    private Comment parentComment;
    
    private boolean approved = true;
}