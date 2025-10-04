package com.anas.groupservice.client;

import com.anas.groupservice.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "chat-service")
public interface ChatServiceClient {

    @GetMapping("/api/v1/users")
    List<UserResponse> getAllUsers(@RequestHeader("Authorization") String authorization);
}