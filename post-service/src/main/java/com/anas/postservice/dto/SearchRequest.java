package com.anas.postservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @NotBlank(message = "Search query cannot be empty")
    private String query;
    
    private String groupId;
}