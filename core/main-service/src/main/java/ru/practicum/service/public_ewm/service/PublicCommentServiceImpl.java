package ru.practicum.service.public_ewm.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.service.dal.CommentLikeRepository;
import ru.practicum.service.dal.CommentRepository;
import ru.practicum.service.dto.CommentDto;
import ru.practicum.service.mapper.CommentMapper;
import ru.practicum.service.model.Comment;
import ru.practicum.service.model.CommentLike;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class PublicCommentServiceImpl implements PublicCommentService {
	final CommentRepository commentRepository;
	final CommentLikeRepository commentLikeRepository;

	@Override
	public List<CommentDto> getCommentByEventId(Long eventId, Integer from, Integer size) {
		log.info("PublicCommentServiceImpl: Поиск комментов с заданными параметрами");
		Pageable pageable = PageRequest.of(from / size, size);
		List<Comment> commentList = commentRepository.findByEvent_IdOrderByCreatedOnDesc(eventId, pageable);
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
