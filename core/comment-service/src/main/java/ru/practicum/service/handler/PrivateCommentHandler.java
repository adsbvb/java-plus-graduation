package ru.practicum.service.handler;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.EventClient;
import ru.practicum.client.UserClient;
import ru.practicum.dal.CommentLikeRepository;
import ru.practicum.dal.CommentRepository;
import ru.practicum.dto.CommentDto;
import ru.practicum.dto.CommentRequestDto;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.enums.State;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentLike;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateCommentHandler {
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final UserClient userClient;
    private final EventClient eventClient;

    @Transactional
    public CommentDto createComment(Long userId, Long eventId, CommentRequestDto commentRequestDto) {
        UserShortDto user = getUserById(userId);
        EventFullDto event = getEventById(eventId);

        if (event.getState().equals(State.PENDING)) {
            throw new NotFoundException("Такого события не найдено.");
        }
        if (event.getInitiatorDto().getId().equals(userId)) {
            throw new ConflictException("Нельзя комментировать свое событие.");
        }

        return CommentMapper
                .commentToCommentDto(
                        commentRepository.save(CommentMapper.commentDtoToComment(commentRequestDto, userId, eventId)));
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, CommentRequestDto commentRequestDto) {
        UserShortDto user = getUserById(userId);
        EventFullDto event = getEventById(eventId);

        if (event.getInitiatorDto().getId().equals(userId)) {
            throw new ConflictException("Нельзя комментировать свое событие.");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден."));

        comment.setText(commentRequestDto.getText());

        return CommentMapper.commentToCommentDto(commentRepository.save(comment));
    }

    @Transactional
    public void deleteComment(Long userId, Long eventId, Long commentId) {
        UserShortDto user = getUserById(userId);
        EventFullDto event = getEventById(eventId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден."));
        if (comment.getAuthorId().equals(userId)) {
            commentRepository.delete(comment);
        } else {
            throw new ConflictException("Невозможно удалить чужой комментарий.");
        }
    }

    @Transactional
    public void addAndDeleteLikeComment(Long userId, Long eventId, Long commentId) {
        UserShortDto user = getUserById(userId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден."));
        if (userId.equals(comment.getAuthorId())) {
            throw new ConflictException("Невозможно поставить лайк на свой комментарий.");
        }

        CommentLike like = CommentLike.builder()
                .userId(userId)
                .commentId(commentId)
                .build();
        boolean exists = commentLikeRepository.existsByCommentIdAndUserId(commentId, userId);
        if (!exists) {
            commentLikeRepository.save(like);
        } else {
            commentLikeRepository.delete(like);
        }
    }

    private UserShortDto getUserById(Long userId) {
        try {
            return userClient.getUserByIdInternal(userId);
        } catch (FeignException.NotFound ex) {
            log.warn(ex.getMessage());
            throw new NotFoundException("Пользователь не найден, id: " + userId);
        } catch (FeignException ex) {
            log.error(ex.getMessage());
            throw new ServiceException("Ошибка при вызове сервиса пользователей", ex);
        }
    }

    private EventFullDto getEventById(Long eventId) {
        try {
            return eventClient.getEventInternal(eventId);
        } catch (FeignException.NotFound ex) {
            log.warn(ex.getMessage());
            throw new NotFoundException("Событие не найдено, id: " + eventId);
        } catch (FeignException ex) {
            log.error(ex.getMessage());
            throw new ServiceException("Ошибка при вызове сервиса событий", ex);
        }
    }
}
