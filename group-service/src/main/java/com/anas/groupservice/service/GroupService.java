package com.anas.groupservice.service;

import com.anas.groupservice.dto.CreateGroupRequest;
import com.anas.groupservice.dto.GroupDTO;
import com.anas.groupservice.dto.NotificationDTO;
import com.anas.groupservice.entity.Group;
import com.anas.groupservice.entity.GroupMember;
import com.anas.groupservice.repository.GroupRepository;
import com.anas.groupservice.repository.GroupMemberRepository;
import com.anas.groupservice.mapper.GroupMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupMapper groupMapper;
    private final NotificationService notificationService;

    public GroupDTO createGroup(CreateGroupRequest request, String creatorId) {
        Group group = new Group();
        group.setName(request.getName());
        group.setDescription(request.getDescription());
        group.setSubject(request.getSubject());
        group.setArchived(false);

        Group savedGroup = groupRepository.save(group);

        // Add creator as admin
        groupMemberRepository.save(createGroupAdmin(savedGroup, creatorId));

        // Send notification
        NotificationDTO notification = new NotificationDTO();
        notification.setType("GROUP_CREATED");
        notification.setGroupId(savedGroup.getId().toString());
        notification.setGroupName(savedGroup.getName());
        notification.setMessage("Group '" + savedGroup.getName() + "' has been created");
        notification.setUserId(creatorId);
        notification.setTimestamp(LocalDateTime.now());
        
        notificationService.sendNotificationToAll(notification);

        return groupMapper.toDTO(savedGroup);
    }

    public List<GroupDTO> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<GroupDTO> searchGroups(String teacherId, String subject, Boolean archived, String keyword) {
        return groupRepository.searchGroups(teacherId, subject, archived, keyword).stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<GroupDTO> getGroupsByTeacherId(String teacherId) {
        return groupRepository.findGroupsByTeacherId(teacherId).stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<GroupDTO> getActiveGroupsByUserId(String userId) {
        return groupRepository.findActiveGroupsByUserId(userId).stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<GroupDTO> getArchivedGroupsByUserId(String userId) {
        return groupRepository.findArchivedGroupsByUserId(userId).stream()
                .map(groupMapper::toDTO)
                .collect(Collectors.toList());
    }

    public GroupDTO getGroupById(Long id) {
        return groupRepository.findById(id)
                .map(groupMapper::toDTO)
                .orElse(null);
    }

    public GroupDTO updateGroup(Long id, GroupDTO groupDTO) {
        return groupRepository.findById(id)
                .map(group -> {
                    group.setName(groupDTO.getName());
                    group.setDescription(groupDTO.getDescription());
                    group.setSubject(groupDTO.getSubject());
                    group.setArchived(groupDTO.isArchived());
                    return groupMapper.toDTO(groupRepository.save(group));
                })
                .orElse(null);
    }

    public void deleteGroup(Long id) {
        Group group = groupRepository.findById(id).orElse(null);
        if (group != null) {
            groupRepository.deleteById(id);
            
            // Send notification
            NotificationDTO notification = new NotificationDTO();
            notification.setType("GROUP_DELETED");
            notification.setGroupId(id.toString());
            notification.setGroupName(group.getName());
            notification.setMessage("Group '" + group.getName() + "' has been deleted");
            notification.setTimestamp(LocalDateTime.now());
            
            notificationService.sendNotificationToAll(notification);
        }
    }

    public void archiveGroup(Long id) {
        groupRepository.findById(id).ifPresent(group -> {
            group.setArchived(true);
            Group savedGroup = groupRepository.save(group);
            
            // Send notification
            NotificationDTO notification = new NotificationDTO();
            notification.setType("GROUP_ARCHIVED");
            notification.setGroupId(id.toString());
            notification.setGroupName(group.getName());
            notification.setMessage("Group '" + group.getName() + "' has been archived");
            notification.setTimestamp(LocalDateTime.now());
            
            notificationService.sendNotificationToAll(notification);
        });
    }

    private GroupMember createGroupAdmin(Group group, String userId) {
        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUserId(userId);
        groupMember.setAdmin(true);
        groupMember.setCoAdmin(false);
        groupMember.setStatus("ACTIVE");
        return groupMember;
    }
}