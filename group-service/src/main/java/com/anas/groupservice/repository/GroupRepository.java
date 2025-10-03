package com.anas.groupservice.repository;

import com.anas.groupservice.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    
    @Query("SELECT g FROM Group g WHERE g.code = :code AND g.active = true")
    Optional<Group> findByCode(@Param("code") String code);
    
    @Query("SELECT g FROM Group g WHERE g.createdBy = :userId AND g.active = true")
    List<Group> findByCreatedBy(@Param("userId") String userId);
    
    @Query("SELECT g FROM Group g WHERE :userId IN ELEMENTS(g.memberIds) AND g.active = true")
    List<Group> findByMemberId(@Param("userId") String userId);
    
    @Query("SELECT g FROM Group g WHERE g.name LIKE %:name% AND g.active = true")
    List<Group> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT CASE WHEN :userId IN ELEMENTS(g.memberIds) THEN true ELSE false END FROM Group g WHERE g.id = :groupId")
    boolean isMember(@Param("groupId") Long groupId, @Param("userId") String userId);
    
    @Query("SELECT CASE WHEN :userId IN ELEMENTS(g.adminIds) THEN true ELSE false END FROM Group g WHERE g.id = :groupId")
    boolean isAdmin(@Param("groupId") Long groupId, @Param("userId") String userId);
}