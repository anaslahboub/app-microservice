package com.anas.groupservice.service;

import com.anas.groupservice.dto.GroupStatisticsDTO;
import com.anas.groupservice.entity.Group;
import com.anas.groupservice.repository.GroupRepository;
import com.anas.groupservice.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;

    public GroupStatisticsDTO getGroupStatistics() {
        GroupStatisticsDTO stats = new GroupStatisticsDTO();
        
        List<Group> allGroups = groupRepository.findAll();
        
        stats.setTotalGroups((long) allGroups.size());
        
        long activeGroups = allGroups.stream().filter(g -> !g.isArchived()).count();
        stats.setActiveGroups(activeGroups);
        
        long archivedGroups = allGroups.stream().filter(Group::isArchived).count();
        stats.setArchivedGroups(archivedGroups);
        
        long totalMembers = groupMemberRepository.count();
        stats.setTotalMembers(totalMembers);
        
        if (!allGroups.isEmpty()) {
            double average = (double) totalMembers / allGroups.size();
            stats.setAverageMembersPerGroup(Math.round(average * 100.0) / 100.0);
        }
        
        // Find the group with the most members
        allGroups.stream()
            .max((g1, g2) -> {
                long count1 = groupMemberRepository.countActiveMembersByGroupId(g1.getId());
                long count2 = groupMemberRepository.countActiveMembersByGroupId(g2.getId());
                return Long.compare(count1, count2);
            })
            .ifPresent(group -> {
                stats.setMostPopularGroupName(group.getName());
                stats.setMostPopularGroupMemberCount(groupMemberRepository.countActiveMembersByGroupId(group.getId()));
            });
        
        return stats;
    }
    
    public GroupStatisticsDTO getGroupStatisticsByTeacher(String teacherId) {
        List<Group> teacherGroups = groupRepository.findGroupsByTeacherId(teacherId);
        
        GroupStatisticsDTO stats = new GroupStatisticsDTO();
        stats.setTotalGroups((long) teacherGroups.size());
        
        long activeGroups = teacherGroups.stream().filter(g -> !g.isArchived()).count();
        stats.setActiveGroups(activeGroups);
        
        long archivedGroups = teacherGroups.stream().filter(Group::isArchived).count();
        stats.setArchivedGroups(archivedGroups);
        
        long totalMembers = teacherGroups.stream()
            .mapToLong(g -> groupMemberRepository.countActiveMembersByGroupId(g.getId()))
            .sum();
        stats.setTotalMembers(totalMembers);
        
        if (!teacherGroups.isEmpty()) {
            double average = (double) totalMembers / teacherGroups.size();
            stats.setAverageMembersPerGroup(Math.round(average * 100.0) / 100.0);
        }
        
        // Find the group with the most members
        teacherGroups.stream()
            .max((g1, g2) -> {
                long count1 = groupMemberRepository.countActiveMembersByGroupId(g1.getId());
                long count2 = groupMemberRepository.countActiveMembersByGroupId(g2.getId());
                return Long.compare(count1, count2);
            })
            .ifPresent(group -> {
                stats.setMostPopularGroupName(group.getName());
                stats.setMostPopularGroupMemberCount(groupMemberRepository.countActiveMembersByGroupId(group.getId()));
            });
        
        return stats;
    }
}