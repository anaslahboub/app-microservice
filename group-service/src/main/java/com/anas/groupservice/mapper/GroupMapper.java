package com.anas.groupservice.mapper;

import com.anas.groupservice.dto.GroupDTO;
import com.anas.groupservice.entity.Group;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public GroupDTO toDTO(Group group) {
        if (group == null) {
            return null;
        }

        GroupDTO dto = new GroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setSubject(group.getSubject());
        dto.setArchived(group.isArchived());
        dto.setMemberCount((long) group.getMemberCount());
        dto.setCreatedBy(group.getCreatedBy());
        dto.setCreatedDate(group.getCreatedDate());

        return dto;
    }

    public Group toEntity(GroupDTO dto) {
        if (dto == null) {
            return null;
        }

        Group group = new Group();
        group.setId(dto.getId());
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setSubject(dto.getSubject());
        group.setArchived(dto.isArchived());

        return group;
    }
}