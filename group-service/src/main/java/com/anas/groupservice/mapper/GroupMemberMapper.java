package com.anas.groupservice.mapper;

import com.anas.groupservice.dto.GroupMemberDTO;
import com.anas.groupservice.entity.GroupMember;
import org.springframework.stereotype.Component;

@Component
public class GroupMemberMapper {

    public GroupMemberDTO toDTO(GroupMember groupMember) {
        if (groupMember == null) {
            return null;
        }

        GroupMemberDTO dto = new GroupMemberDTO();
        dto.setId(groupMember.getId());
        dto.setGroupId(groupMember.getGroup() != null ? groupMember.getGroup().getId() : null);
        dto.setUserId(groupMember.getUserId());
        dto.setAdmin(groupMember.isAdmin());
        dto.setCoAdmin(groupMember.isCoAdmin());
        dto.setStatus(groupMember.getStatus());
        dto.setCreatedBy(groupMember.getCreatedBy());
        dto.setCreatedDate(groupMember.getCreatedDate());

        return dto;
    }

    public GroupMember toEntity(GroupMemberDTO dto) {
        if (dto == null) {
            return null;
        }

        GroupMember groupMember = new GroupMember();
        groupMember.setId(dto.getId());
        
        // Map group if groupId is provided
        if (dto.getGroupId() != null) {
            com.anas.groupservice.entity.Group group = new com.anas.groupservice.entity.Group();
            group.setId(dto.getGroupId());
            groupMember.setGroup(group);
        }
        
        groupMember.setUserId(dto.getUserId());
        groupMember.setAdmin(dto.isAdmin());
        groupMember.setCoAdmin(dto.isCoAdmin());
        groupMember.setStatus(dto.getStatus());

        return groupMember;
    }
}