package com.anas.groupservice.controller;

import com.anas.groupservice.dto.AddMemberRequest;
import com.anas.groupservice.dto.GroupMemberDTO;
import com.anas.groupservice.service.GroupMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/members")
@RequiredArgsConstructor
public class GroupMemberController {

    private final GroupMemberService groupMemberService;

    @PostMapping
    public ResponseEntity<GroupMemberDTO> addMember(@PathVariable Long groupId, 
                                                    @RequestBody AddMemberRequest request,
                                                    Authentication authentication) {
        GroupMemberDTO member = groupMemberService.addMember(groupId, request, authentication.getName());
        return ResponseEntity.ok(member);
    }

    @GetMapping
    public ResponseEntity<List<GroupMemberDTO>> getGroupMembers(@PathVariable Long groupId) {
        List<GroupMemberDTO> members = groupMemberService.getGroupMembers(groupId);
        return ResponseEntity.ok(members);
    }

    @GetMapping("/students")
    public ResponseEntity<List<GroupMemberDTO>> getStudentsByGroupId(@PathVariable Long groupId) {
        List<GroupMemberDTO> students = groupMemberService.getStudentsByGroupId(groupId);
        return ResponseEntity.ok(students);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(@PathVariable Long groupId,
                                             @PathVariable String userId,
                                             Authentication authentication) {
        groupMemberService.removeMember(groupId, userId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/leave")
    public ResponseEntity<Void> leaveGroup(@PathVariable Long groupId,
                                           @PathVariable String userId,
                                           Authentication authentication) {
        // Verify that the authenticated user is the same as the userId
        if (!authentication.getName().equals(userId)) {
            return ResponseEntity.status(403).build();
        }
        
        groupMemberService.leaveGroup(groupId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/co-admin")
    public ResponseEntity<GroupMemberDTO> designateCoAdmin(@PathVariable Long groupId,
                                                           @PathVariable String userId,
                                                           Authentication authentication) {
        GroupMemberDTO member = groupMemberService.designateCoAdmin(groupId, userId, authentication.getName());
        return ResponseEntity.ok(member);
    }
}