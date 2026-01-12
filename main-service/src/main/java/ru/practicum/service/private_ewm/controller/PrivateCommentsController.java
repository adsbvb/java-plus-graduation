package ru.practicum.service.private_ewm.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.dto.CommentDto;
import ru.practicum.service.dto.CommentRequestDto;
import ru.practicum.service.private_ewm.service.PrivateServiceComments;

@Slf4j
@Controller
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events/{eventId}/comments")
public class PrivateCommentsController {
    private PrivateServiceComments privateServiceComments;

    @PostMapping
    public ResponseEntity<CommentDto> createComment(@PathVariable(value = "userId") Long userId,
                                                    @PathVariable(value = "eventId") Long eventId,
                                                    @RequestBody @Valid CommentRequestDto commentRequestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(privateServiceComments.createComment(userId, eventId, commentRequestDto));
    }

    @PostMapping("/{commentId}/likes")
    public ResponseEntity<Void> addAndDeleteLikeComment(@PathVariable(value = "userId") Long userId,
                                               @PathVariable(value = "eventId") Long eventId,
                                               @PathVariable(value = "commentId") Long commentId) {
        privateServiceComments.addAndDeleteLikeComment(userId, eventId, commentId);
        return ResponseEntity.ok().body(null);
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<CommentDto> updateComment(@PathVariable(value = "userId") Long userId,
                                              @PathVariable(value = "eventId") Long eventId,
                                              @PathVariable(value = "commentId") Long commentId,
                                              @RequestBody @Valid CommentRequestDto commentRequestDto) {
        return ResponseEntity.ok()
                .body(privateServiceComments.updateComment(userId, eventId, commentId, commentRequestDto));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable(value = "userId") Long userId,
                                              @PathVariable(value = "eventId") Long eventId,
                                              @PathVariable(value = "commentId") Long commentId) {
        privateServiceComments.deleteComment(userId, eventId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }
}
