package com.anas.postservice.dto;

import com.anas.postservice.entities.Bookmark;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BookmarkResult {
    private Bookmark bookmark;
    private boolean bookmarked;
    private Long bookmarkCount;
    private String action;
}