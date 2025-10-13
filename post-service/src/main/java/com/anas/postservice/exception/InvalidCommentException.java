package com.anas.postservice.exception;

public class InvalidCommentException extends RuntimeException {

    public InvalidCommentException(String commentContentCannotBeEmpty) {
        super(commentContentCannotBeEmpty);
    }
}
