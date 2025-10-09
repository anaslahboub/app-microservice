package com.anas.postservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PostRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @NotBlank(message = "Post content cannot be empty")
    @Size(max = 5000, message = "Post content cannot exceed 5000 characters")
    private String content;
    
    private String imageUrl;
    
    @NotBlank(message = "Group ID is required")
    private String groupId;
    

    private MultipartFile imageFile;
}