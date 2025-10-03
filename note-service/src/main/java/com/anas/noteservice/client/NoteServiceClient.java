package com.anas.noteservice.client;

import com.anas.noteservice.model.Note;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "note-service", url = "http://localhost:8084")
public interface NoteServiceClient {
    
    @GetMapping("/api/v1/notes")
    List<Note> getNotesByUser(@RequestParam("userId") String userId);
    
    @GetMapping("/api/v1/notes/group/{groupId}")
    List<Note> getNotesByUserAndGroup(@PathVariable("groupId") String groupId, @RequestParam("userId") String userId);
    
    @GetMapping("/api/v1/notes/chat/{chatId}")
    List<Note> getNotesByUserAndChat(@PathVariable("chatId") String chatId, @RequestParam("userId") String userId);
    
    @GetMapping("/api/v1/notes/pinned")
    List<Note> getPinnedNotesByUser(@RequestParam("userId") String userId);
    
    @GetMapping("/api/v1/notes/count")
    Long getNoteCountByUser(@RequestParam("userId") String userId);
}