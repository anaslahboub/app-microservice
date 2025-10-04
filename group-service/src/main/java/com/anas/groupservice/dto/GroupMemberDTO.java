package com.anas.groupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupMemberDTO {
    private Long id;
    private Long groupId;
    private String userId;
    private boolean isAdmin;
    private boolean coAdmin;
    private String status;
    private String createdBy;
    private LocalDateTime createdDate;
}