package com.anas.groupservice.controller;

import com.anas.groupservice.dto.GroupMemberDTO;
import com.anas.groupservice.service.GroupMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final GroupMemberService groupMemberService;

    @GetMapping
    public ResponseEntity<List<GroupMemberDTO>> getAllStudents() {
        List<GroupMemberDTO> students = groupMemberService.getAllStudents();
        return ResponseEntity.ok(students);
    }
}