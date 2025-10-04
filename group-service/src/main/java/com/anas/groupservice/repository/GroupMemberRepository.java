package com.anas.groupservice.repository;

import com.anas.groupservice.entity.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    List<GroupMember> findByGroupId(Long groupId);

    List<GroupMember> findByUserId(String userId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.userId = :userId")
    Optional<GroupMember> findByGroupIdAndUserId(@Param("groupId") Long groupId, @Param("userId") String userId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.isAdmin = false AND gm.isCoAdmin = false")
    List<GroupMember> findStudentsByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.isAdmin = false AND gm.isCoAdmin = false")
    List<GroupMember> findAllStudents();

    @Query("SELECT DISTINCT gm.userId FROM GroupMember gm WHERE gm.isAdmin = false AND gm.isCoAdmin = false")
    List<String> findAllStudentUserIds();

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.isAdmin = true")
    List<GroupMember> findAdminsByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.status = 'ACTIVE'")
    List<GroupMember> findActiveMembersByGroupId(@Param("groupId") Long groupId);

    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.status = 'ACTIVE'")
    Long countActiveMembersByGroupId(@Param("groupId") Long groupId);
}