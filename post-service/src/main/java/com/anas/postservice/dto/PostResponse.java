package com.anas.postservice.dto;

import com.anas.postservice.enumeration.PostStatus;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
@Data @Builder @AllArgsConstructor  @NoArgsConstructor
public class PostResponse {
    private Long id;
    private String content;
    private String imageUrl;
    private PostStatus status;
    private String authorId;
    private String authorName; // Ajouté depuis User
    private Long likeCount;
    private Long commentCount;
    private Long bookmarkCount;
    private boolean pinned;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;

    // Pas de relations lazy-loaded
}