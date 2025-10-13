package com.anas.postservice.config;

import com.anas.postservice.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;
    
    @Value("${application.file.uploads.post-output-path:./post-uploads}")
    private String fileUploadPath;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/v1/posts/**")
                .addPathPatterns("/api/v1/files/**");
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Ensure the path ends with a slash and is properly formatted for URLs
        Path path = Paths.get(fileUploadPath).toAbsolutePath().normalize();
        File directory = path.toFile();
        
        // Create directory if it doesn't exist
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Convert to proper file URL format
        String fileLocation = "file:///" + directory.getAbsolutePath().replace("\\", "/") + "/";
        
        System.out.println("Serving static files from: " + fileLocation);
        
        // Serve files from the post-uploads directory
        registry.addResourceHandler("/post-uploads/**")
                .addResourceLocations(fileLocation);
    }
}