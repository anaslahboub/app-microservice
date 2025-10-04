package com.anas.groupservice.service;

import com.anas.groupservice.client.ChatServiceClient;
import com.anas.groupservice.dto.GroupMemberDTO;
import com.anas.groupservice.dto.UserResponse;
import com.anas.groupservice.entity.GroupMember;
import com.anas.groupservice.mapper.GroupMemberMapper;
import com.anas.groupservice.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final GroupMemberRepository groupMemberRepository;
    private final GroupMemberMapper groupMemberMapper;
    private final ChatServiceClient chatServiceClient;

    public List<UserResponse> getAllUsers(String authToken) {
        // Get all users from chat service
        return chatServiceClient.getAllUsers(authToken);
    }

    public void assignRole(String userId, String role) {
        // In a real implementation, this would integrate with Keycloak or your auth service
        // For now, we'll simulate by updating a user's status or creating a record
        // This is a simplified implementation - in practice, you'd call Keycloak's Admin API
        
        GroupMember member = groupMemberRepository.findByUserId(userId)
                .stream()
                .findFirst()
                .orElseGet(() -> {
                    // Create a new member record if one doesn't exist
                    GroupMember newMember = new GroupMember();
                    newMember.setUserId(userId);
                    newMember.setStatus("ACTIVE");
                    return groupMemberRepository.save(newMember);
                });
                
        // In a real implementation, you would call Keycloak's Admin API here
        // For now, we're just updating the local record
        switch (role.toUpperCase()) {
            case "ADMIN":
                member.setAdmin(true);
                member.setCoAdmin(false);
                break;
            case "TEACHER":
                member.setAdmin(false);
                member.setCoAdmin(true);
                break;
            case "STUDENT":
                member.setAdmin(false);
                member.setCoAdmin(false);
                break;
            default:
                throw new IllegalArgumentException("Invalid role: " + role);
        }
        
        groupMemberRepository.save(member);
    }

    public void deleteUser(String userId) {
        // In a real implementation, this would delete the user from Keycloak
        // For now, we'll just mark them as inactive in our system
        List<GroupMember> members = groupMemberRepository.findByUserId(userId);
        for (GroupMember member : members) {
            member.setStatus("DELETED");
            groupMemberRepository.save(member);
        }
    }

    public GroupMemberDTO createUser(GroupMemberDTO userDto) {
        // In a real implementation, this would create a user in Keycloak
        // For now, we'll just create a local record
        GroupMember member = groupMemberMapper.toEntity(userDto);
        member.setStatus("ACTIVE");
        GroupMember savedMember = groupMemberRepository.save(member);
        return groupMemberMapper.toDTO(savedMember);
    }
}