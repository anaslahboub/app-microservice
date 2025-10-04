package com.anas.groupservice.dto;

import com.anas.groupservice.entity.GroupPostState;
import com.anas.groupservice.entity.GroupPostType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupPostDTO {
    private Long id;
    private Long groupId;
    private String userId;
    private String content;
    private GroupPostType type;
    private GroupPostState state;
    private String filePath;
    private String fileName;
    private String createdBy;
    private LocalDateTime createdDate;
}