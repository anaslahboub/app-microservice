package com.anas.groupservice.controller;

import com.anas.groupservice.dto.FileDownloadDTO;
import com.anas.groupservice.dto.GroupPostDTO;
import com.anas.groupservice.dto.UploadFileRequest;
import com.anas.groupservice.service.GroupPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/posts")
@RequiredArgsConstructor
public class GroupPostController {

    private final GroupPostService groupPostService;

    @PostMapping
    public ResponseEntity<GroupPostDTO> createPost(
            @PathVariable Long groupId,
            @RequestBody GroupPostDTO postDTO,
            Authentication authentication) {
        
        // Set the group ID and user ID from the request
        postDTO.setGroupId(groupId);
        postDTO.setUserId(authentication.getName());
        
        GroupPostDTO createdPost = groupPostService.createPost(postDTO);
        return ResponseEntity.ok(createdPost);
    }

    @PostMapping("/upload")
    public ResponseEntity<GroupPostDTO> uploadFile(
            @PathVariable Long groupId,
            @RequestParam("file") MultipartFile file,
            @ModelAttribute UploadFileRequest request,
            Authentication authentication) {
        
        GroupPostDTO createdPost = groupPostService.uploadFile(groupId, request, file, authentication.getName());
        return ResponseEntity.ok(createdPost);
    }

    @GetMapping
    public ResponseEntity<List<GroupPostDTO>> getPublishedPosts(@PathVariable Long groupId) {
        List<GroupPostDTO> posts = groupPostService.getPublishedPostsByGroupId(groupId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<GroupPostDTO>> getPublishedPostsByUser(
            @PathVariable Long groupId,
            @PathVariable String userId) {
        List<GroupPostDTO> posts = groupPostService.getPublishedPostsByGroupIdAndUserId(groupId, userId);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/{postId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long groupId, @PathVariable Long postId) {
        FileDownloadDTO fileDownload = groupPostService.downloadFile(postId);
        
        ByteArrayResource resource = new ByteArrayResource(fileDownload.getData());
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileDownload.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileDownload.getFileName() + "\"")
                .body(resource);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long groupId,
            @PathVariable Long postId,
            Authentication authentication) {
        
        // In a real implementation, you would check if the user has permission to delete the post
        // (e.g., they are the post author or a group admin)
        
        groupPostService.deletePost(postId);
        return ResponseEntity.noContent().build();
    }
}