package com.anas.groupservice.client;

import com.anas.groupservice.model.Group;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "group-service", url = "http://localhost:8083")
public interface GroupServiceClient {
    
    @GetMapping("/api/v1/groups/{id}")
    Group getGroupById(@PathVariable("id") Long id);
    
    @GetMapping("/api/v1/groups/code/{code}")
    Group getGroupByCode(@PathVariable("code") String code);
    
    @GetMapping("/api/v1/groups/my")
    List<Group> getMyGroups(@RequestParam("userId") String userId);
    
    @GetMapping("/api/v1/groups/{id}/member")
    boolean isMember(@PathVariable("id") Long id, @RequestParam("userId") String userId);
    
    @GetMapping("/api/v1/groups/{id}/admin")
    boolean isAdmin(@PathVariable("id") Long id, @RequestParam("userId") String userId);
}