package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/internal/requests")
public class InternalRequestController {
    private final RequestService service;

    @GetMapping("/event/{eventId}")
    List<ParticipationRequestDto> getRequestsByEventIdInternal(
            @PathVariable(name = "eventId") Long eventId) {
        return service.getRequestsByEventId(eventId);
    }

    @GetMapping("/by-ids")
    List<ParticipationRequestDto> getRequestsByIdsInternal(
            @RequestParam("ids") List<Long> ids
    ) {
        return service.getRequestsByIds(ids);
    }

    @PutMapping("/batch")
    public List<ParticipationRequestDto> batchUpdateRequestsIternal(
            @RequestBody List<ParticipationRequestDto> requests
    ) {
        return service.batchUpdateRequests(requests);
    }
}
