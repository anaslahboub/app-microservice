package com.anas.postservice.dto;

import com.anas.postservice.entities.Like;
import lombok.*;

@NoArgsConstructor @AllArgsConstructor @Getter  @Setter @Builder
public class LikeResponse {
    private Like like;
    private boolean liked;
    private String action;
    private Long likeCount;
}
