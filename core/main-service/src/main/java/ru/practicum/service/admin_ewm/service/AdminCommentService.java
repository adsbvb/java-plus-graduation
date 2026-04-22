package ru.practicum.service.admin_ewm.service;

import org.springframework.data.domain.Pageable;
import ru.practicum.service.dto.CommentAdminDto;

import java.util.List;

public interface AdminCommentService {
    List<CommentAdminDto> getAllComments(Long eventId, Long userId, String text, Pageable pageable);

    CommentAdminDto getCommentById(Long commentId);

    void deleteComment(Long commentId);
}
