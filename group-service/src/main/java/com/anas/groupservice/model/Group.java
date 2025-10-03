package com.anas.groupservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Group {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String createdBy;
    private List<String> memberIds;
    private List<String> adminIds;
    private boolean active;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}