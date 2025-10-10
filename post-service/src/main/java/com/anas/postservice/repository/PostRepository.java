package com.anas.postservice.repository;

import com.anas.postservice.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {



    @Query("SELECT p FROM Post p ORDER BY p.createdDate DESC")
    Page<Post> findAllOrderByCreatedDateDesc(Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.authorId = :authorId ORDER BY p.createdDate DESC")
    Page<Post> findPostsByAuthorId(@Param("authorId") String authorId, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.status = 'PENDING' ORDER BY p.createdDate ASC")
    Page<Post> findPendingPosts(Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.authorId = :authorId AND p.status = 'PENDING' ORDER BY p.createdDate DESC")
    Page<Post> findPendingPostsByAuthorId(@Param("authorId") String authorId, Pageable pageable);
    
    @Query("SELECT p FROM Post p WHERE p.status = 'APPROVED' ORDER BY p.likeCount DESC, p.commentCount DESC, p.createdDate DESC")
    Page<Post> findTrendingPosts(Pageable pageable);
    
    @Query("SELECT COUNT(l) FROM Like l WHERE l.post.id = :postId AND l.userId = :userId")
    Long countByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") String userId);
    

    
    @Query("SELECT p FROM Post p WHERE p.status = 'APPROVED' AND (LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))) ORDER BY p.createdDate DESC")
    Page<Post> searchApprovedPosts(@Param("query") String query, Pageable pageable);

    Post getPostById(Long id);
}