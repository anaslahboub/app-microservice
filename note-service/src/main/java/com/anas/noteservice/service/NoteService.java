package com.anas.noteservice.service;

import com.anas.noteservice.entity.Note;
import com.anas.noteservice.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {
    
    private final NoteRepository noteRepository;
    
    @Transactional
    public Note createNote(String title, String content, String userId, String groupId, String chatId) {
        Note note = new Note();
        note.setTitle(title);
        note.setContent(content);
        note.setUserId(userId);
        note.setGroupId(groupId);
        note.setChatId(chatId);
        
        return noteRepository.save(note);
    }
    
    public Page<Note> getNotesByUser(String userId, Pageable pageable) {
        return noteRepository.findByUserId(userId, pageable);
    }
    
    public Page<Note> getNotesByUserAndGroup(String userId, String groupId, Pageable pageable) {
        return noteRepository.findByUserIdAndGroupId(userId, groupId, pageable);
    }
    
    public List<Note> getNotesByUserAndChat(String userId, String chatId) {
        return noteRepository.findByUserIdAndChatId(userId, chatId);
    }
    
    public Page<Note> searchNotesByUser(String userId, String searchTerm, Pageable pageable) {
        return noteRepository.findByUserIdAndSearchTerm(userId, searchTerm, pageable);
    }
    
    public List<Note> getPinnedNotesByUser(String userId) {
        return noteRepository.findPinnedByUserId(userId);
    }
    
    public Long getNoteCountByUser(String userId) {
        return noteRepository.countByUserId(userId);
    }
    
    @Transactional
    public Note updateNote(Long noteId, String title, String content, String userId, Boolean pinned) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
                
        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("You don't have permission to update this note");
        }
        
        if (title != null) {
            note.setTitle(title);
        }
        
        if (content != null) {
            note.setContent(content);
        }
        
        if (pinned != null) {
            note.setPinned(pinned);
        }
        
        return noteRepository.save(note);
    }
    
    @Transactional
    public void deleteNote(Long noteId, String userId) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
                
        if (!note.getUserId().equals(userId)) {
            throw new RuntimeException("You don't have permission to delete this note");
        }
        
        noteRepository.deleteById(noteId);
    }
    
    public Note getNoteById(Long noteId) {
        return noteRepository.findById(noteId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
    }
}