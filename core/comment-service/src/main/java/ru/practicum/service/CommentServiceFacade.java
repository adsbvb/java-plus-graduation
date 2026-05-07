package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.dto.CommentAdminDto;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentRequestDto;
import ru.practicum.service.handler.AdminCommentHandler;
import ru.practicum.service.handler.PrivateCommentHandler;
import ru.practicum.service.handler.PublicCommentHandler;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceFacade implements CommentService {
    private final AdminCommentHandler adminCommentHandler;
    private final PrivateCommentHandler privateCommentHandler;
    private final PublicCommentHandler publicCommentHandler;

    @Override
    public List<CommentAdminDto> getAllComments(Long eventId, Long userId, String text, Pageable pageable) {
        return adminCommentHandler.getAllComments(eventId, userId, text, pageable);
    }

    @Override
    public CommentAdminDto getCommentById(Long commentId) {
        return adminCommentHandler.getCommentById(commentId);
    }

    @Override
    public void deleteComment(Long commentId) {
        adminCommentHandler.deleteComment(commentId);
    }

    @Override
    public CommentDto createComment(Long userId, Long eventId, CommentRequestDto comment) {
        return privateCommentHandler.createComment(userId, eventId, comment);
    }

    @Override
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, CommentRequestDto comment) {
        return privateCommentHandler.updateComment(userId, eventId, commentId, comment);
    }

    @Override
    public void deleteComment(Long userId, Long eventId, Long commentId) {
        privateCommentHandler.deleteComment(userId, eventId, commentId);
    }

    @Override
    public void addAndDeleteLikeComment(Long userId, Long eventId, Long commentId) {
        privateCommentHandler.addAndDeleteLikeComment(userId, eventId, commentId);
    }

    @Override
    public List<CommentDto> getCommentByEventId(Long eventId, Integer from, Integer size) {
        return publicCommentHandler.getCommentByEventId(eventId, from, size);
    }
}
