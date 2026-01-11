package ru.practicum.service.private_ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.service.dal.CommentRepository;
import ru.practicum.service.dal.EventRepository;
import ru.practicum.service.dal.UserRepository;
import ru.practicum.service.dto.CommentDto;
import ru.practicum.service.dto.CommentRequestDto;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.model.Event;
import ru.practicum.service.model.User;

@Service
@RequiredArgsConstructor
public class PrivateServiceCommentImpl implements PrivateServiceComments {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    @Override
    public CommentDto createComment(Long userId, Long eventId, CommentRequestDto comment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));
        return null;
    }

    @Override
    public CommentDto updateComment(Long userId, Long eventId, Long commentId, CommentRequestDto comment) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Такого пользователя не существует."));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Такого события не найдено."));
        return null;
    }

    @Override
    public void deleteComment(Long userId, Long eventId, Long commentId) {

    }

    @Override
    public void addLikeComment(Long userId, Long eventId, Long commentId) {

    }
}
