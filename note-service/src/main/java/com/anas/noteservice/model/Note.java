package com.anas.noteservice.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Note {
    private Long id;
    private String title;
    private String content;
    private String userId;
    private String groupId;
    private String chatId;
    private boolean pinned;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}