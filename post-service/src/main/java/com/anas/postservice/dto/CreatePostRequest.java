package com.anas.postservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

// DTO
@Data
@Builder
public class CreatePostRequest {
    @NotBlank(message = "Content is required")
    @Size(max = 5000, message = "Content must not exceed 5000 characters")
    private String content;

    // For pre-existing image URLs
    private String imageUrl;

    // For new image uploads
    private MultipartFile imageFile;        // Optional
}