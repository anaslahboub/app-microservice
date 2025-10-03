package com.anas.postservice.repository;

import com.anas.postservice.entities.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    
    @Query("SELECT v FROM Vote v WHERE v.post.id = :postId AND v.userId = :userId")
    Optional<Vote> findByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") String userId);
    
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.post.id = :postId AND v.upvote = true")
    Long countUpvotesByPostId(@Param("postId") Long postId);
    
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.post.id = :postId AND v.upvote = false")
    Long countDownvotesByPostId(@Param("postId") Long postId);
    
    @Query("SELECT COUNT(v) FROM Vote v WHERE v.post.id = :postId")
    Long countVotesByPostId(@Param("postId") Long postId);
}