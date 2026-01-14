package ru.practicum.service.admin_ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.service.dal.CommentRepository;
import ru.practicum.service.dto.CommentAdminDto;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.mapper.CommentMapper;
import ru.practicum.service.model.Comment;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminCommentServiceImpl implements AdminCommentService {
    private final CommentRepository commentRepository;

    @Override
    public List<CommentAdminDto> getAllComments(Long eventId, Long userId, String text, Pageable pageable) {

        List<Comment> comments;

        if (eventId != null && userId != null) {
            comments = commentRepository.findAllByEventIdAndAuthorIdOrderByCreatedOnDesc(eventId, userId, pageable);
        } else if (eventId != null) {
            comments = commentRepository.findByEvent_IdOrderByCreatedOnDesc(eventId, pageable);
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

    @Override
    public CommentAdminDto getCommentById(Long commentId) {
        Comment comment =  findCommentOrThrow(commentId);
        log.info("Найден комментарий: {}",  comment);
        return CommentMapper.commentToCommentAdminDto(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long commentId) {
        Comment comment = findCommentOrThrow(commentId);
        commentRepository.delete(comment);
        log.info("Комментарий с id={} успешно удален",  commentId);
    }

    private Comment findCommentOrThrow(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.error("Комментарий с id: {} не найден", commentId);
                    return new NotFoundException("Комментарий с id: " + commentId + " не найден");
                });
    }

}
