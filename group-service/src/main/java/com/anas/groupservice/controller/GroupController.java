package com.anas.groupservice.controller;

import com.anas.groupservice.entity.Group;
import com.anas.groupservice.service.GroupService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
@Tag(name = "Group")
public class GroupController {
    
    private final GroupService groupService;
    
    @PostMapping
    public ResponseEntity<Group> createGroup(
            @RequestParam("name") String name,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        
        String userId = authentication.getName();
        Group group = groupService.createGroup(name, description, userId);
        return ResponseEntity.ok(group);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Group> getGroupById(@PathVariable("id") Long id) {
        return groupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/code/{code}")
    public ResponseEntity<Group> getGroupByCode(@PathVariable("code") String code) {
        return groupService.getGroupByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/my")
    public ResponseEntity<List<Group>> getMyGroups(Authentication authentication) {
        String userId = authentication.getName();
        List<Group> createdGroups = groupService.getGroupsCreatedBy(userId);
        List<Group> memberGroups = groupService.getGroupsForMember(userId);
        
        // Combine and deduplicate
        memberGroups.removeAll(createdGroups);
        createdGroups.addAll(memberGroups);
        
        return ResponseEntity.ok(createdGroups);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(
            @PathVariable("id") Long id,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "description", required = false) String description,
            Authentication authentication) {
        
        String userId = authentication.getName();
        Group group = groupService.updateGroup(id, name, description, userId);
        return ResponseEntity.ok(group);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable("id") Long id, Authentication authentication) {
        String userId = authentication.getName();
        groupService.deleteGroup(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/join/{code}")
    public ResponseEntity<Group> joinGroup(@PathVariable("code") String code, Authentication authentication) {
        String userId = authentication.getName();
        Group group = groupService.joinGroup(code, userId);
        return ResponseEntity.ok(group);
    }
    
    @PostMapping("/{id}/leave")
    public ResponseEntity<Group> leaveGroup(@PathVariable("id") Long id, Authentication authentication) {
        String userId = authentication.getName();
        Group group = groupService.leaveGroup(id, userId);
        return ResponseEntity.ok(group);
    }
    
    @PostMapping("/{id}/admin/{adminId}")
    public ResponseEntity<Group> addAdmin(
            @PathVariable("id") Long id,
            @PathVariable("adminId") String adminId,
            Authentication authentication) {
        
        String userId = authentication.getName();
        Group group = groupService.addAdmin(id, userId, adminId);
        return ResponseEntity.ok(group);
    }
    
    @DeleteMapping("/{id}/admin/{adminId}")
    public ResponseEntity<Group> removeAdmin(
            @PathVariable("id") Long id,
            @PathVariable("adminId") String adminId,
            Authentication authentication) {
        
        String userId = authentication.getName();
        Group group = groupService.removeAdmin(id, userId, adminId);
        return ResponseEntity.ok(group);
    }
    
    @GetMapping("/{id}/member")
    public ResponseEntity<Boolean> isMember(@PathVariable("id") Long id, Authentication authentication) {
        String userId = authentication.getName();
        boolean isMember = groupService.isMember(id, userId);
        return ResponseEntity.ok(isMember);
    }
    
    @GetMapping("/{id}/admin")
    public ResponseEntity<Boolean> isAdmin(@PathVariable("id") Long id, Authentication authentication) {
        String userId = authentication.getName();
        boolean isAdmin = groupService.isAdmin(id, userId);
        return ResponseEntity.ok(isAdmin);
    }
}