package com.anas.groupservice.controller;

import com.anas.groupservice.dto.CreateGroupRequest;
import com.anas.groupservice.dto.GroupDTO;
import com.anas.groupservice.dto.SearchRequest;
import com.anas.groupservice.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<GroupDTO> createGroup(@RequestBody CreateGroupRequest request, Authentication authentication) {
        GroupDTO group = groupService.createGroup(request, authentication.getName());
        return ResponseEntity.ok(group);
    }

    @GetMapping
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        List<GroupDTO> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/teacher/{teacherId}")
    public ResponseEntity<List<GroupDTO>> getGroupsByTeacherId(@PathVariable String teacherId) {
        List<GroupDTO> groups = groupService.getGroupsByTeacherId(teacherId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupDTO>> getGroupsByUserId(@PathVariable String userId) {
        List<GroupDTO> groups = groupService.getActiveGroupsByUserId(userId);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GroupDTO> getGroupById(@PathVariable Long id) {
        GroupDTO group = groupService.getGroupById(id);
        return group != null ? ResponseEntity.ok(group) : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<GroupDTO> updateGroup(@PathVariable Long id, @RequestBody GroupDTO groupDTO, Authentication authentication) {
        // Check permissions before updating
        GroupDTO updatedGroup = groupService.updateGroup(id, groupDTO);
        return updatedGroup != null ? ResponseEntity.ok(updatedGroup) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id, Authentication authentication) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public ResponseEntity<List<GroupDTO>> searchGroups(@RequestBody SearchRequest searchRequest) {
        List<GroupDTO> groups = groupService.searchGroups(
                searchRequest.getTeacherId(),
                searchRequest.getSubject(),
                searchRequest.getArchived(),
                searchRequest.getKeyword()
        );
        return ResponseEntity.ok(groups);
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Void> archiveGroup(@PathVariable Long id, Authentication authentication) {
        groupService.archiveGroup(id);
        return ResponseEntity.noContent().build();
    }
}