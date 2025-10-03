package com.anas.postservice.client;

import com.anas.postservice.model.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "chat-service", url = "http://localhost:8081", configuration = com.anas.postservice.config.FeignConfig.class)
public interface UserServiceClient {
    
    @GetMapping("/api/v1/users")
    List<User> getAllUsers();
    
    @GetMapping("/api/v1/users/{id}")
    User getUserById(@PathVariable("id") String id);
}