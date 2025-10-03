package com.anas.postservice.repository;

import com.anas.postservice.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.approved = true ORDER BY c.createdDate ASC")
    Page<Comment> findApprovedCommentsByPostId(@Param("postId") Long postId, Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL AND c.approved = true ORDER BY c.createdDate ASC")
    Page<Comment> findRootCommentsByPostId(@Param("postId") Long postId, Pageable pageable);
    
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId AND c.approved = true ORDER BY c.createdDate ASC")
    List<Comment> findRepliesByParentCommentId(@Param("parentCommentId") Long parentCommentId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.approved = true")
    Long countApprovedCommentsByPostId(@Param("postId") Long postId);
}