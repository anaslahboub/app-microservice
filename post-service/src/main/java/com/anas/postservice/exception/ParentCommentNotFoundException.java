package com.anas.postservice.exception;

public class ParentCommentNotFoundException extends RuntimeException {
    public ParentCommentNotFoundException(Long commentId) {
        super("Parent comment not found with id: " + commentId);
    }
}