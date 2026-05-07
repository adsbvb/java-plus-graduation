package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.CommentService;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentRequestDto;

@Slf4j
@Controller
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events/{eventId}/comments")
public class PrivateCommentsController {
    private final CommentService service;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@PathVariable(value = "userId") Long userId,
                                                    @PathVariable(value = "eventId") Long eventId,
                                                    @RequestBody @Valid CommentRequestDto commentRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createComment(userId, eventId, commentRequestDto));
    }

    @PostMapping("/{commentId}/likes")
    public ResponseEntity<Void> addAndDeleteLikeComment(@PathVariable(value = "userId") Long userId,
                                               @PathVariable(value = "eventId") Long eventId,
                                               @PathVariable(value = "commentId") Long commentId) {
        service.addAndDeleteLikeComment(userId, eventId, commentId);
        return ResponseEntity.ok().body(null);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable(value = "userId") Long userId,
                                              @PathVariable(value = "eventId") Long eventId,
                                              @PathVariable(value = "commentId") Long commentId,
                                              @RequestBody @Valid CommentRequestDto commentRequestDto) {
        return ResponseEntity.ok()
                .body(service.updateComment(userId, eventId, commentId, commentRequestDto));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable(value = "userId") Long userId,
                                              @PathVariable(value = "eventId") Long eventId,
                                              @PathVariable(value = "commentId") Long commentId) {
        service.deleteComment(userId, eventId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
