package ru.practicum.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.dto.CommentAdminDto;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentRequestDto;

import java.util.List;

public interface CommentService {
    List<CommentAdminDto> getAllComments(Long eventId, Long userId, String text, Pageable pageable);

    CommentAdminDto getCommentById(Long commentId);

    void deleteComment(Long commentId);

    CommentDto createComment(Long userId, Long eventId, CommentRequestDto comment);

    CommentDto updateComment(Long userId, Long eventId, Long commentId, CommentRequestDto comment);

    void deleteComment(Long userId, Long eventId, Long commentId);

    void addAndDeleteLikeComment(Long userId, Long eventId, Long commentId);

    List<CommentDto> getCommentByEventId(Long eventId, Integer from, Integer size);
}
