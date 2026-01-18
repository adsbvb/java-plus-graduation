package ru.practicum.service.public_ewm.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.dto.CommentDto;
import ru.practicum.service.public_ewm.service.PublicCommentService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/events/{eventId}/comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class PublicCommentController {
	final PublicCommentService commentService;

	@GetMapping
	public List<CommentDto> getCommentForEvent(@PathVariable(value = "eventId") Long eventId,
											   @RequestParam(value = "from", defaultValue = "0") Integer from,
											   @RequestParam(value = "size", defaultValue = "10") Integer size) {
		log.info("PublicCommentController: Вызов эндпоинта GET /events/{}/comments, с параметрами запроса from:{}, size:{}",
				eventId, from, size);

		return commentService.getCommentByEventId(eventId, from, size);
	}

}
