package ru.practicum.service.private_ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.service.dal.CommentLikeRepository;
import ru.practicum.service.dal.CommentRepository;
import ru.practicum.service.dal.EventRepository;
import ru.practicum.service.dal.UserRepository;
import ru.practicum.service.dto.CommentDto;
import ru.practicum.service.dto.CommentRequestDto;
import ru.practicum.service.error.ConflictException;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.mapper.CommentMapper;
import ru.practicum.service.model.Comment;
import ru.practicum.service.model.CommentLike;
import ru.practicum.service.model.Event;
import ru.practicum.service.model.User;
import ru.practicum.service.model.enums.State;

@Service
@RequiredArgsConstructor
public class PrivateServiceCommentImpl implements PrivateServiceComments {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Override
    public CommentDto createComment(Long userId, Long eventId, CommentRequestDto commentRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));
        if (event.getState().equals(State.PENDING)) {
            throw new NotFoundException("Такого события не найдено.");
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Нельзя комментировать свое событие.");
        }

        return CommentMapper
                .commentToCommentDto(
                        commentRepository.save(CommentMapper.commentDtoToComment(commentRequestDto, user, event)));
    }

    @Override
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, CommentRequestDto commentRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Нельзя комментировать свое событие.");
        }
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден."));

        comment.setText(commentRequestDto.getText());

        return CommentMapper.commentToCommentDto(commentRepository.save(comment));
    }

    @Override
    public void deleteComment(Long userId, Long eventId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден."));
        if (comment.getAuthor().getId().equals(userId)) {
            commentRepository.delete(comment);
        } else {
            throw new ConflictException("Невозможно удалить чужой комментарий.");
        }
    }

    @Override
    public void addAndDeleteLikeComment(Long userId, Long eventId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует."));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден."));
        if (userId.equals(comment.getAuthor().getId())) {
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
}
