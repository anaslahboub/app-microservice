package com.anas.groupservice.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    
    public String getName() {
        return firstName + " " + lastName;
    }
}