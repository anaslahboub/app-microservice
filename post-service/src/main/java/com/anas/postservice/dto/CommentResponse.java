package com.anas.postservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {
    private Long id;
    private String content;
    private String authorId;
    private Long postId;
    private Long parentCommentId;
    private boolean isReply;
    private LocalDateTime createdDate;
    private List<CommentResponse> replies;
}