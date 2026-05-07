package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UserShortDto;
import ru.practicum.event.service.EventService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/events")
public class InternalEventController {
    private final EventService eventService;

    @PostMapping("/event/{id}/query")
    public EventFullDto getEventInternal(
            @PathVariable(name = "id") Long eventId,
            @RequestBody UserShortDto dto) {

        return eventService.getEventByIdIternal(eventId, dto);
    }

    @PutMapping("/event/{eventId}/confirmed/increment")
    public void incrementConfirmedRequestsInternal(
            @PathVariable Long eventId,
            @RequestParam int count) {

        eventService.incrementConfirmedRequestsInternal(eventId, count);
    }
}
