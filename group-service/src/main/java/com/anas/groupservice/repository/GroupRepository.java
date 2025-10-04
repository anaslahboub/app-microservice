package com.anas.groupservice.repository;

import com.anas.groupservice.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    @Query("SELECT g FROM Group g JOIN g.groupMembers gm WHERE gm.userId = :userId AND g.archived = false")
    List<Group> findActiveGroupsByUserId(@Param("userId") String userId);

    @Query("SELECT g FROM Group g JOIN g.groupMembers gm WHERE gm.userId = :userId AND g.archived = true")
    List<Group> findArchivedGroupsByUserId(@Param("userId") String userId);

    @Query("SELECT g FROM Group g WHERE g.createdBy = :teacherId")
    List<Group> findGroupsByTeacherId(@Param("teacherId") String teacherId);

    @Query("SELECT g FROM Group g WHERE g.subject = :subject AND g.archived = false")
    List<Group> findActiveGroupsBySubject(@Param("subject") String subject);

    List<Group> findByArchived(boolean archived);

    @Query("SELECT g FROM Group g WHERE " +
           "(:teacherId IS NULL OR g.createdBy = :teacherId) AND " +
           "(:subject IS NULL OR g.subject = :subject) AND " +
           "(:archived IS NULL OR g.archived = :archived) AND " +
           "(:keyword IS NULL OR LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(g.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Group> searchGroups(@Param("teacherId") String teacherId, 
                            @Param("subject") String subject, 
                            @Param("archived") Boolean archived, 
                            @Param("keyword") String keyword);
}