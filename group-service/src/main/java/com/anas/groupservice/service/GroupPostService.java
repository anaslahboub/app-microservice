package com.anas.groupservice.service;

import com.anas.groupservice.dto.FileDownloadDTO;
import com.anas.groupservice.dto.GroupPostDTO;
import com.anas.groupservice.dto.UploadFileRequest;
import com.anas.groupservice.entity.Group;
import com.anas.groupservice.entity.GroupPost;
import com.anas.groupservice.entity.GroupPostState;
import com.anas.groupservice.entity.GroupPostType;
import com.anas.groupservice.mapper.GroupPostMapper;
import com.anas.groupservice.repository.GroupPostRepository;
import com.anas.groupservice.repository.GroupRepository;
import com.anas.groupservice.util.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupPostService {

    private final GroupPostRepository groupPostRepository;
    private final GroupRepository groupRepository;
    private final GroupPostMapper groupPostMapper;
    private final FileService fileService;

    public GroupPostDTO createPost(GroupPostDTO postDTO) {
        Group group = groupRepository.findById(postDTO.getGroupId())
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        GroupPost groupPost = groupPostMapper.toEntity(postDTO);
        groupPost.setGroup(group);
        groupPost.setState(GroupPostState.PUBLISHED);

        GroupPost savedPost = groupPostRepository.save(groupPost);
        return groupPostMapper.toDTO(savedPost);
    }

    public GroupPostDTO uploadFile(Long groupId, UploadFileRequest request, MultipartFile file, String userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        // Save the file
        String filePath = fileService.saveFile(file, groupId, userId);
        
        if (filePath == null) {
            throw new RuntimeException("Failed to save file");
        }

        // Determine file type based on the file extension
        GroupPostType fileType = determineFileType(file.getOriginalFilename());

        // Create the group post
        GroupPost groupPost = new GroupPost();
        groupPost.setGroup(group);
        groupPost.setUserId(userId);
        groupPost.setContent(request.getContent());
        groupPost.setType(fileType);
        groupPost.setState(GroupPostState.PUBLISHED);
        groupPost.setFilePath(filePath);
        groupPost.setFileName(file.getOriginalFilename());

        GroupPost savedPost = groupPostRepository.save(groupPost);
        return groupPostMapper.toDTO(savedPost);
    }

    public List<GroupPostDTO> getPublishedPostsByGroupId(Long groupId) {
        return groupPostRepository.findPublishedPostsByGroupId(groupId)
                .stream()
                .map(groupPostMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<GroupPostDTO> getPublishedPostsByGroupIdAndUserId(Long groupId, String userId) {
        return groupPostRepository.findPublishedPostsByGroupIdAndUserId(groupId, userId)
                .stream()
                .map(groupPostMapper::toDTO)
                .collect(Collectors.toList());
    }

    public FileDownloadDTO downloadFile(Long postId) {
        GroupPost post = groupPostRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));
        
        if (post.getFilePath() == null || post.getFilePath().isEmpty()) {
            throw new IllegalArgumentException("No file associated with this post");
        }
        
        byte[] fileData = FileUtils.readFileFromLocation(post.getFilePath());
        String contentType = FileUtils.getContentType(post.getFileName());
        
        FileDownloadDTO downloadDTO = new FileDownloadDTO();
        downloadDTO.setData(fileData);
        downloadDTO.setFileName(post.getFileName());
        downloadDTO.setContentType(contentType);
        
        return downloadDTO;
    }

    public void deletePost(Long postId) {
        groupPostRepository.findById(postId).ifPresent(post -> {
            post.setState(GroupPostState.DELETED);
            groupPostRepository.save(post);
        });
    }
    
    private GroupPostType determineFileType(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return GroupPostType.TEXT;
        }
        
        String extension = getFileExtension(fileName).toLowerCase();
        
        switch (extension) {
            case "jpg":
            case "jpeg":
            case "png":
            case "gif":
                return GroupPostType.IMAGE;
            case "mp4":
            case "avi":
            case "mov":
                return GroupPostType.VIDEO;
            case "mp3":
            case "wav":
                return GroupPostType.AUDIO;
            case "pdf":
            case "doc":
            case "docx":
            case "txt":
                return GroupPostType.DOCUMENT;
            default:
                return GroupPostType.DOCUMENT;
        }
    }
    
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1);
    }
}