package ru.practicum.controller;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.CommentDto;
import ru.practicum.service.CommentService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/events/{eventId}/comments")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class PublicCommentController {
	final CommentService service;

	@GetMapping
	public List<CommentDto> getCommentForEvent(@PathVariable(value = "eventId") Long eventId,
											   @RequestParam(value = "from", defaultValue = "0") Integer from,
											   @RequestParam(value = "size", defaultValue = "10") Integer size) {
		log.info("PublicCommentController: Вызов эндпоинта GET /events/{}/comments, с параметрами запроса from:{}, size:{}",
				eventId, from, size);

		return service.getCommentByEventId(eventId, from, size);
	}

}
