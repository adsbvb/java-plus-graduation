package ru.practicum.service.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import ru.practicum.dal.CommentLikeRepository;
import ru.practicum.dal.CommentRepository;
import ru.practicum.dto.CommentDto;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.CommentLike;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PublicCommentHandler {
    final CommentRepository commentRepository;
    final CommentLikeRepository commentLikeRepository;

    public List<CommentDto> getCommentByEventId(Long eventId, Integer from, Integer size) {
        log.info("PublicCommentServiceImpl: Поиск комментов с заданными параметрами");
        Pageable pageable = PageRequest.of(from / size, size);
        List<Comment> commentList = commentRepository.findByEventIdOrderByCreatedOnDesc(eventId, pageable);
        log.info("PublicCommentServiceImpl: {}", commentList);

        log.info("PublicCommentServiceImpl: Поиск лайков комментов");
        List<Long> commentsIds = commentList.stream().map(Comment::getId).toList();
        List<CommentLike> commentLikeList = commentLikeRepository.findByCommentIdIn(commentsIds);

        Map<Long, Integer> commentLikesMap = commentLikeList.stream()
                .collect(Collectors.groupingBy(CommentLike::getCommentId, Collectors.summingInt(like -> 1)));
        log.info("PublicCommentServiceImpl: {}", commentLikesMap);

        return commentList.stream()
                .map(comment ->
                        CommentMapper.toCommentDtoWithLikes(comment, commentLikesMap.getOrDefault(comment.getId(), 0)))
                .toList();
    }
}
