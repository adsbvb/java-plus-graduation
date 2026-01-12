package ru.practicum.service.private_ewm.service;

import ru.practicum.service.dto.CommentDto;
import ru.practicum.service.dto.CommentRequestDto;

public interface PrivateServiceComments {
    CommentDto createComment(Long userId, Long eventId, CommentRequestDto comment);

    CommentDto updateComment(Long userId, Long eventId, Long commentId, CommentRequestDto comment);

    void deleteComment(Long userId, Long eventId, Long commentId);

    void addAndDeleteLikeComment(Long userId, Long eventId, Long commentId);
}
