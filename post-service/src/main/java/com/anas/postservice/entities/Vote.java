package com.anas.postservice.entities;


import com.anas.postservice.common.BaseAuditingEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import static jakarta.persistence.GenerationType.SEQUENCE;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "votes")
public class Vote extends BaseAuditingEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @SequenceGenerator(name = "vote_id_seq", sequenceName = "vote_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = SEQUENCE, generator = "vote_id_seq")
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    @JsonIgnore
    private Post post;
    
    // Store user ID directly instead of JPA relationship
    private String userId;
    
    private boolean upvote;
}