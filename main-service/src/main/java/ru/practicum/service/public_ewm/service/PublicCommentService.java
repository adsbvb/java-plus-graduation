package ru.practicum.service.public_ewm.service;

import ru.practicum.service.dto.CommentDto;

import java.util.List;

public interface PublicCommentService {
	List<CommentDto> getCommentByEventId(Long eventId, Integer from, Integer size);
}
