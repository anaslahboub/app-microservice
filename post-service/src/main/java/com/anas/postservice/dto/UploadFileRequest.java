package com.anas.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private MultipartFile file;
    private Long postId;
    private String description;
}