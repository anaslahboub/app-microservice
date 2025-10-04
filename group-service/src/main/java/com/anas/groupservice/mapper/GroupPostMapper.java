package com.anas.groupservice.mapper;

import com.anas.groupservice.dto.GroupPostDTO;
import com.anas.groupservice.entity.GroupPost;
import org.springframework.stereotype.Component;

@Component
public class GroupPostMapper {

    public GroupPostDTO toDTO(GroupPost groupPost) {
        if (groupPost == null) {
            return null;
        }

        GroupPostDTO dto = new GroupPostDTO();
        dto.setId(groupPost.getId());
        dto.setGroupId(groupPost.getGroup() != null ? groupPost.getGroup().getId() : null);
        dto.setUserId(groupPost.getUserId());
        dto.setContent(groupPost.getContent());
        dto.setType(groupPost.getType());
        dto.setState(groupPost.getState());
        dto.setFilePath(groupPost.getFilePath());
        dto.setFileName(groupPost.getFileName());
        dto.setCreatedBy(groupPost.getCreatedBy());
        dto.setCreatedDate(groupPost.getCreatedDate());

        return dto;
    }

    public GroupPost toEntity(GroupPostDTO dto) {
        if (dto == null) {
            return null;
        }

        GroupPost groupPost = new GroupPost();
        groupPost.setId(dto.getId());
        
        // Map group if groupId is provided
        if (dto.getGroupId() != null) {
            com.anas.groupservice.entity.Group group = new com.anas.groupservice.entity.Group();
            group.setId(dto.getGroupId());
            groupPost.setGroup(group);
        }
        
        groupPost.setUserId(dto.getUserId());
        groupPost.setContent(dto.getContent());
        groupPost.setType(dto.getType());
        groupPost.setState(dto.getState());
        groupPost.setFilePath(dto.getFilePath());
        groupPost.setFileName(dto.getFileName());

        return groupPost;
    }
}