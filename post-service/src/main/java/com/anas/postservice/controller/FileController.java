package com.anas.postservice.controller;

import com.anas.postservice.dto.UploadFileRequest;
import com.anas.postservice.file.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
@Tag(name = "File")
public class FileController {

    private final FileService fileService;

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("post-id") Long postId,
            Authentication authentication) {

        String userId = authentication.getName();
        String filePath = fileService.saveFile(file, postId, userId);

        if (filePath != null) {
            return ResponseEntity.ok(filePath);
        } else {
            return ResponseEntity.status(500).body("File upload failed");
        }
    }


}