package com.anas.postservice.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private String content;
    private Long parentCommentId;
}