package ru.practicum.service.admin_ewm.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.admin_ewm.service.AdminCommentService;
import ru.practicum.service.dto.CommentAdminDto;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminCommentController {
    private final AdminCommentService service;

    @GetMapping
    public List<CommentAdminDto> findAll(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String text,
            @RequestParam(defaultValue = "0") Integer from,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        log.info("GET /admin/comments with params: eventId={}, userId={} , text={}", eventId, userId, text);
        Pageable pageable = PageRequest.of(from / size, size);
        return service.getAllComments(eventId, userId, text, pageable);
    }

    @GetMapping("/{commentId}")
    public CommentAdminDto findOne(
            @PathVariable(name = "commentId") Long commentId
    ) {
        log.info("GET /admin/comments/{}", commentId);
        return service.getCommentById(commentId);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(
            @PathVariable(name = "commentId") @Positive Long commentId) {
        log.info("DELETE /admin/comment/{}", commentId);
        service.deleteComment(commentId);
    }
}