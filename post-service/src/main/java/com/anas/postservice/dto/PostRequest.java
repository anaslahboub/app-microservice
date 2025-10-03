package com.anas.postservice.dto;

import lombok.Data;

@Data
public class PostRequest {
    private String content;
    private String imageUrl;
    private String groupId;
}