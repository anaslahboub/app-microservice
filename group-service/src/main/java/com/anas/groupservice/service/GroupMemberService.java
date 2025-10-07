package com.anas.groupservice.service;

import com.anas.groupservice.dto.AddMemberRequest;
import com.anas.groupservice.dto.GroupMemberDTO;
import com.anas.groupservice.dto.NotificationDTO;
import com.anas.groupservice.entity.Group;
import com.anas.groupservice.entity.GroupMember;
import com.anas.groupservice.repository.GroupMemberRepository;
import com.anas.groupservice.repository.GroupRepository;
import com.anas.groupservice.mapper.GroupMemberMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupMemberService {

    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberMapper groupMemberMapper;
    private final NotificationService notificationService;

    public GroupMemberDTO addMember(Long groupId, AddMemberRequest request, String requesterId) {
        // Check if requester is admin or co-admin
        if (!isUserAdminOrCoAdmin(groupId, requesterId)) {
            throw new SecurityException("Only admin or co-admin can add members");
        }

        // Check if user is already a member
        Optional<GroupMember> existingMember = groupMemberRepository
                .findByGroupIdAndUserId(groupId, request.getUserId());
        if (existingMember.isPresent()) {
            throw new IllegalArgumentException("User is already a member of this group");
        }

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        GroupMember groupMember = new GroupMember();
        groupMember.setGroup(group);
        groupMember.setUserId(request.getUserId());
        groupMember.setAdmin(request.isAdmin());
        groupMember.setCoAdmin(request.getCoAdmin());
        groupMember.setStatus("ACTIVE");

        GroupMember savedMember = groupMemberRepository.save(groupMember);
        
        // Send notification
        Group groupEntity = groupRepository.findById(groupId).orElse(null);
        if (groupEntity != null) {
            NotificationDTO notification = new NotificationDTO();
            notification.setType("MEMBER_ADDED");
            notification.setGroupId(groupId.toString());
            notification.setGroupName(groupEntity.getName());
            notification.setMessage("User has been added to group '" + groupEntity.getName() + "'");
            notification.setUserId(request.getUserId());
            notification.setTimestamp(LocalDateTime.now().toString());
            notification.setRead(false);
            
            notificationService.createAndSendUserNotification(request.getUserId(), notification);
        }
        
        return groupMemberMapper.toDTO(savedMember);
    }

    public List<GroupMemberDTO> getGroupMembers(Long groupId) {
        return groupMemberRepository.findByGroupId(groupId).stream()
                .map(groupMemberMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<GroupMemberDTO> getStudentsByGroupId(Long groupId) {
        return groupMemberRepository.findStudentsByGroupId(groupId).stream()
                .map(groupMemberMapper::toDTO)
                .collect(Collectors.toList());
    }

    public void removeMember(Long groupId, String userId, String requesterId) {
        if (!isUserAdminOrCoAdmin(groupId, requesterId)) {
            throw new SecurityException("Only admin or co-admin can remove members");
        }

        // Empêcher l'admin de se supprimer lui-même
        if (requesterId.equals(userId) && isUserAdmin(groupId, userId)) {
            throw new IllegalArgumentException("Admin cannot remove themselves");
        }

        // Chercher le membre dans la base
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .ifPresent(groupMember -> {
                    // Supprimer le membre de la base
                    groupMemberRepository.delete(groupMember);

                    // Envoyer la notification
                    Group group = groupRepository.findById(groupId).orElse(null);
                    if (group != null) {
                        NotificationDTO notification = new NotificationDTO();
                        notification.setType("MEMBER_REMOVED");
                        notification.setGroupId(groupId.toString());
                        notification.setGroupName(group.getName());
                        notification.setMessage("User has been removed from group '" + group.getName() + "'");
                        notification.setUserId(userId);
                        notification.setTimestamp(LocalDateTime.now().toString());
                        notification.setRead(false);

                        notificationService.createAndSendUserNotification(userId, notification);
                    }
                });
    }

    public void leaveGroup(Long groupId, String userId) {
        groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .ifPresent(groupMember -> {
                    groupMember.setStatus("LEFT");
                    GroupMember savedMember = groupMemberRepository.save(groupMember);
                    
                    // Send notification
                    Group group = groupRepository.findById(groupId).orElse(null);
                    if (group != null) {
                        NotificationDTO notification = new NotificationDTO();
                        notification.setType("MEMBER_LEFT");
                        notification.setGroupId(groupId.toString());
                        notification.setGroupName(group.getName());
                        notification.setMessage("User has left group '" + group.getName() + "'");
                        notification.setUserId(userId);
                        notification.setTimestamp(LocalDateTime.now().toString());
                        notification.setRead(false);
                        
                        // Notify group members about the user leaving
                        notificationService.createAndSendGroupNotification(groupId.toString(), notification);
                    }
                });
    }

    public GroupMemberDTO designateCoAdmin(Long groupId, String userId, String requesterId) {
        // Check if requester is admin
        if (!isUserAdmin(groupId, requesterId)) {
            throw new SecurityException("Only admin can designate co-admin");
        }

        GroupMember groupMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new IllegalArgumentException("User is not a member of this group"));

        groupMember.setCoAdmin(true);
        GroupMember updatedMember = groupMemberRepository.save(groupMember);
        
        // Send notification
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group != null) {
            NotificationDTO notification = new NotificationDTO();
            notification.setType("CO_ADMIN_ASSIGNED");
            notification.setGroupId(groupId.toString());
            notification.setGroupName(group.getName());
            notification.setMessage("User has been designated as co-admin for group '" + group.getName() + "'");
            notification.setUserId(userId);
            notification.setTimestamp(LocalDateTime.now().toString());
            notification.setRead(false);
            
            notificationService.createAndSendUserNotification(userId, notification);
        }
        
        return groupMemberMapper.toDTO(updatedMember);
    }

    public boolean isUserAdminOrCoAdmin(Long groupId, String userId) {
        Optional<GroupMember> groupMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        return groupMember.isPresent() && (groupMember.get().isAdmin() || groupMember.get().isCoAdmin());
    }

    public boolean isUserAdmin(Long groupId, String userId) {
        Optional<GroupMember> groupMember = groupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        return groupMember.isPresent() && groupMember.get().isAdmin();
    }

    public boolean isUserMember(Long groupId, String userId) {
        return groupMemberRepository.findByGroupIdAndUserId(groupId, userId).isPresent();
    }
}