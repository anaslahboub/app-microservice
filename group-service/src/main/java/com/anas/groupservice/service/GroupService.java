package com.anas.groupservice.service;

import com.anas.groupservice.entity.Group;
import com.anas.groupservice.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {
    
    private final GroupRepository groupRepository;
    
    @Transactional
    public Group createGroup(String name, String description, String createdBy) {
        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        group.setCreatedBy(createdBy);
        
        // Generate unique code
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (groupRepository.findByCode(code).isPresent());
        
        group.setCode(code);
        
        // Add creator as both member and admin
        group.getMemberIds().add(createdBy);
        group.getAdminIds().add(createdBy);
        
        return groupRepository.save(group);
    }
    
    public Optional<Group> getGroupById(Long id) {
        return groupRepository.findById(id);
    }
    
    public Optional<Group> getGroupByCode(String code) {
        return groupRepository.findByCode(code);
    }
    
    public List<Group> getGroupsCreatedBy(String userId) {
        return groupRepository.findByCreatedBy(userId);
    }
    
    public List<Group> getGroupsForMember(String userId) {
        return groupRepository.findByMemberId(userId);
    }
    
    @Transactional
    public Group updateGroup(Long groupId, String name, String description, String userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
                
        if (!group.getAdminIds().contains(userId)) {
            throw new RuntimeException("Only admins can update the group");
        }
        
        if (name != null) {
            group.setName(name);
        }
        
        if (description != null) {
            group.setDescription(description);
        }
        
        return groupRepository.save(group);
    }
    
    @Transactional
    public void deleteGroup(Long groupId, String userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
                
        if (!group.getCreatedBy().equals(userId)) {
            throw new RuntimeException("Only the creator can delete the group");
        }
        
        group.setActive(false);
        groupRepository.save(group);
    }
    
    @Transactional
    public Group joinGroup(String code, String userId) {
        Group group = groupRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Group not found"));
                
        if (!group.getMemberIds().contains(userId)) {
            group.getMemberIds().add(userId);
            group = groupRepository.save(group);
        }
        
        return group;
    }
    
    @Transactional
    public Group leaveGroup(Long groupId, String userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
                
        if (group.getCreatedBy().equals(userId)) {
            throw new RuntimeException("Creator cannot leave the group");
        }
        
        group.getMemberIds().remove(userId);
        group.getAdminIds().remove(userId);
        
        return groupRepository.save(group);
    }
    
    @Transactional
    public Group addAdmin(Long groupId, String userId, String adminId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
                
        if (!group.getAdminIds().contains(userId)) {
            throw new RuntimeException("Only admins can add other admins");
        }
        
        if (!group.getMemberIds().contains(adminId)) {
            throw new RuntimeException("User must be a member to become an admin");
        }
        
        if (!group.getAdminIds().contains(adminId)) {
            group.getAdminIds().add(adminId);
            group = groupRepository.save(group);
        }
        
        return group;
    }
    
    @Transactional
    public Group removeAdmin(Long groupId, String userId, String adminId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
                
        if (!group.getAdminIds().contains(userId)) {
            throw new RuntimeException("Only admins can remove other admins");
        }
        
        if (group.getCreatedBy().equals(adminId)) {
            throw new RuntimeException("Cannot remove the group creator from admins");
        }
        
        group.getAdminIds().remove(adminId);
        
        return groupRepository.save(group);
    }
    
    public boolean isMember(Long groupId, String userId) {
        return groupRepository.isMember(groupId, userId);
    }
    
    public boolean isAdmin(Long groupId, String userId) {
        return groupRepository.isAdmin(groupId, userId);
    }
}