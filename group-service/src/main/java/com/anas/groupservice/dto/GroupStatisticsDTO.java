package com.anas.groupservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GroupStatisticsDTO {
    private Long totalGroups;
    private Long activeGroups;
    private Long archivedGroups;
    private Long totalMembers;
    private Double averageMembersPerGroup;
    private Long mostPopularGroupMemberCount;
    private String mostPopularGroupName;
}