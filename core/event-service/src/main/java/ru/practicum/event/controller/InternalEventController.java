package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.event.service.EventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class InternalEventController {
    private final EventService eventService;

    @GetMapping("/event/{id}")
    public EventFullDto getEventInternal(
            @PathVariable(name = "id") Long eventId
    ) {
        return eventService.getEventByIdIternal(eventId);
    }

    @PutMapping("/event/{eventId}/confirmed/increment")
    public void incrementConfirmedRequestsInternal(
            @PathVariable Long eventId,
            @RequestParam int count
    ) {
        eventService.incrementConfirmedRequestsInternal(eventId, count);
    }
}
