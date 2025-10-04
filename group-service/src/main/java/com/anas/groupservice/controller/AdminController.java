package com.anas.groupservice.controller;

import com.anas.groupservice.dto.GroupMemberDTO;
import com.anas.groupservice.dto.UserResponse;
import com.anas.groupservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers(Authentication authentication) {
        // Extract the JWT token from the authentication
        String authToken = "Bearer " + authentication.getCredentials().toString();
        List<UserResponse> users = adminService.getAllUsers(authToken);
        return ResponseEntity.ok(users);
    }

    @PostMapping("/users/{userId}/role")
    public ResponseEntity<Void> assignRole(@PathVariable String userId, @RequestParam String role) {
        adminService.assignRole(userId, role);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users")
    public ResponseEntity<GroupMemberDTO> createUser(@RequestBody GroupMemberDTO userDto) {
        GroupMemberDTO createdUser = adminService.createUser(userDto);
        return ResponseEntity.ok(createdUser);
    }
}