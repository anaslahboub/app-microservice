package com.anas.noteservice.controller;

import com.anas.noteservice.entity.Note;
import com.anas.noteservice.service.NoteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
@Tag(name = "Note")
public class NoteController {
    
    private final NoteService noteService;
    
    @PostMapping
    public ResponseEntity<Note> createNote(
            @RequestParam("title") String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "group-id", required = false) String groupId,
            @RequestParam(value = "chat-id", required = false) String chatId,
            Authentication authentication) {
        
        String userId = authentication.getName();
        Note note = noteService.createNote(title, content, userId, groupId, chatId);
        return ResponseEntity.ok(note);
    }
    
    @GetMapping
    public ResponseEntity<Page<Note>> getNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Note> notes = noteService.getNotesByUser(userId, pageable);
        return ResponseEntity.ok(notes);
    }
    
    @GetMapping("/group/{group-id}")
    public ResponseEntity<Page<Note>> getNotesByGroup(
            @PathVariable("group-id") String groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Note> notes = noteService.getNotesByUserAndGroup(userId, groupId, pageable);
        return ResponseEntity.ok(notes);
    }
    
    @GetMapping("/chat/{chat-id}")
    public ResponseEntity<List<Note>> getNotesByChat(
            @PathVariable("chat-id") String chatId,
            Authentication authentication) {
        
        String userId = authentication.getName();
        List<Note> notes = noteService.getNotesByUserAndChat(userId, chatId);
        return ResponseEntity.ok(notes);
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<Note>> searchNotes(
            @RequestParam("term") String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        
        String userId = authentication.getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Note> notes = noteService.searchNotesByUser(userId, searchTerm, pageable);
        return ResponseEntity.ok(notes);
    }
    
    @GetMapping("/pinned")
    public ResponseEntity<List<Note>> getPinnedNotes(Authentication authentication) {
        String userId = authentication.getName();
        List<Note> notes = noteService.getPinnedNotesByUser(userId);
        return ResponseEntity.ok(notes);
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getNoteCount(Authentication authentication) {
        String userId = authentication.getName();
        Long count = noteService.getNoteCountByUser(userId);
        return ResponseEntity.ok(count);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(
            @PathVariable("id") Long id,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "pinned", required = false) Boolean pinned,
            Authentication authentication) {
        
        String userId = authentication.getName();
        Note note = noteService.updateNote(id, title, content, userId, pinned);
        return ResponseEntity.ok(note);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable("id") Long id, Authentication authentication) {
        String userId = authentication.getName();
        noteService.deleteNote(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable("id") Long id) {
        Note note = noteService.getNoteById(id);
        return ResponseEntity.ok(note);
    }
}