package ru.practicum.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dal.CommentRepository;
import ru.practicum.dto.CommentAdminDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminCommentHandler {
    private final CommentRepository commentRepository;

    public List<CommentAdminDto> getAllComments(Long eventId, Long userId, String text, Pageable pageable) {

        List<Comment> comments;

        if (eventId != null && userId != null) {
            comments = commentRepository.findAllByEventIdAndAuthorIdOrderByCreatedOnDesc(eventId, userId, pageable);
        } else if (eventId != null) {
            comments = commentRepository.findByEventIdOrderByCreatedOnDesc(eventId, pageable);
        } else if (userId != null) {
            comments = commentRepository.findAllByAuthorIdOrderByCreatedOnDesc(userId, pageable);
        } else if (text != null && !text.isEmpty()) {
            comments = commentRepository.findByTextContainingIgnoreCaseOrderByCreatedOnDesc(text, pageable);
        } else {
            comments = commentRepository.findAll(pageable).getContent();
        }

        log.info("Найден список комментариев. Всего: {}", comments.size());
        return comments.stream()
                .map(CommentMapper::commentToCommentAdminDto)
                .toList();
    }

    public CommentAdminDto getCommentById(Long commentId) {
        Comment comment =  findCommentOrThrow(commentId);
        log.info("Найден комментарий: {}",  comment);
        return CommentMapper.commentToCommentAdminDto(comment);
    }

    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = findCommentOrThrow(commentId);
        commentRepository.delete(comment);
        log.info("Комментарий с id={} успешно удален",  commentId);
    }

    private Comment findCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Комментарий с id: {} не найден", commentId);
                    return new NotFoundException("Комментарий с id: " + commentId + " не найден");
                });
    }
}
