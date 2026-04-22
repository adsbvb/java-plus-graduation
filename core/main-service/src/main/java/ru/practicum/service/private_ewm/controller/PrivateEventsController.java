package ru.practicum.service.private_ewm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.dto.*;
import ru.practicum.service.private_ewm.service.PrivateService;

import java.util.List;

@Slf4j
@Controller
@Validated
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/events")
public class PrivateEventsController {
    private final PrivateService service;

    @GetMapping
    public ResponseEntity<List<EventShortDto>> getEventsByOwner(
            @PathVariable(value = "userId") Long userId,
            @RequestParam(value = "from", defaultValue = "0") @PositiveOrZero Long from,
            @RequestParam(value = "size", defaultValue = "10") @PositiveOrZero Long size) {
        return ResponseEntity.ok().body(service.getEventsByOwner(userId, from, size));
    }

    @PostMapping
    public ResponseEntity<EventFullDto> createEvent(@PathVariable(value = "userId") Long userId,
                                                    @Valid @RequestBody NewEventDto newEventDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createEvent(userId, newEventDto));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventFullDto> getInfoEvent(@PathVariable(value = "userId") Long userId,
                                                     @PathVariable(value = "eventId") Long eventId) {
        return ResponseEntity.ok().body(service.getInfoEvent(userId, eventId));
    }

    @PatchMapping("/{eventId}")
    public ResponseEntity<EventFullDto> updateEvent(@PathVariable(value = "userId") Long userId,
                                                    @PathVariable(value = "eventId") Long eventId,
                                                    @Valid @RequestBody UpdateEventUserRequest updateEventUserRequest) {
        return ResponseEntity.ok().body(service.updateEvent(userId, eventId, updateEventUserRequest));
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getInfoRequest(@PathVariable(value = "userId") Long userId,
                                                                  @PathVariable(value = "eventId") Long eventId) {
        return ResponseEntity.ok().body(service.getInfoRequest(userId, eventId));
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateStatusRequest(
            @PathVariable(value = "userId") Long userId,
            @PathVariable(value = "eventId") Long eventId,
            @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        return ResponseEntity.ok().body(service.updateStatusRequest(userId, eventId, updateRequest));
    }

}
