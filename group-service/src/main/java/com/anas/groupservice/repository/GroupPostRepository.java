package com.anas.groupservice.repository;

import com.anas.groupservice.entity.GroupPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupPostRepository extends JpaRepository<GroupPost, Long> {

    @Query("SELECT gp FROM GroupPost gp WHERE gp.group.id = :groupId AND gp.state = 'PUBLISHED' ORDER BY gp.createdDate DESC")
    List<GroupPost> findPublishedPostsByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT gp FROM GroupPost gp WHERE gp.group.id = :groupId AND gp.userId = :userId AND gp.state = 'PUBLISHED' ORDER BY gp.createdDate DESC")
    List<GroupPost> findPublishedPostsByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") String userId);

    @Query("SELECT COUNT(gp) FROM GroupPost gp WHERE gp.group.id = :groupId AND gp.state = 'PUBLISHED'")
    Long countPublishedPostsByGroupId(@Param("groupId") Long groupId);
}