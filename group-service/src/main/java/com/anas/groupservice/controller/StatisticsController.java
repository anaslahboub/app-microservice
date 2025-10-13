package com.anas.groupservice.controller;

import com.anas.groupservice.dto.GroupStatisticsDTO;
import com.anas.groupservice.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/groups/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/groups")
    public ResponseEntity<GroupStatisticsDTO> getGroupStatistics() {
        GroupStatisticsDTO stats = statisticsService.getGroupStatistics();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/groups/teacher")
    public ResponseEntity<GroupStatisticsDTO> getGroupStatisticsForTeacher(Authentication authentication) {
        GroupStatisticsDTO stats = statisticsService.getGroupStatisticsByTeacher(authentication.getName());
        return ResponseEntity.ok(stats);
    }
}