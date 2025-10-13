package com.anas.postservice.controller;

import com.anas.postservice.dto.UploadFileRequest;
import com.anas.postservice.file.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/post/files")
@RequiredArgsConstructor
@Tag(name = "File")
public class FileController {

    private final FileService fileService;
    
    @Value("${application.file.uploads.post-output-path:./post-uploads}")
    private String fileUploadPath;

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
    
    @GetMapping("/debug/info")
    public ResponseEntity<Map<String, Object>> getDebugInfo() {
        Map<String, Object> debugInfo = new HashMap<>();
        
        try {
            Path path = Paths.get(fileUploadPath).toAbsolutePath().normalize();
            File directory = path.toFile();
            
            debugInfo.put("configuredPath", fileUploadPath);
            debugInfo.put("absolutePath", path.toString());
            debugInfo.put("directoryExists", directory.exists());
            debugInfo.put("isDirectory", directory.isDirectory());
            
            if (directory.exists()) {
                File[] files = directory.listFiles();
                debugInfo.put("filesInDirectory", files != null ? files.length : 0);
            }
            
            // Check if a specific file exists
            String testFilePath = "/post-uploads/posts/7/users/caaecc51-b654-428b-a280-2cf3e2d31af3/1759999652321.jpg";
            Path fullPath = Paths.get(".").toAbsolutePath().normalize().resolve(testFilePath.substring(1));
            debugInfo.put("testFileExists", fullPath.toFile().exists());
            debugInfo.put("testFilePath", fullPath.toString());
            
        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(debugInfo);
    }
}