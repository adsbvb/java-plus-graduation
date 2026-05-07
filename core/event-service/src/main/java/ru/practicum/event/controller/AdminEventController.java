package ru.practicum.event.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.event.service.EventService;
import ru.practicum.dto.AdminEventParam;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UpdateEventAdminRequest;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
public class AdminEventController {
    private final EventService service;

    @GetMapping
    public List<EventFullDto> getEvents(
            @Valid @ModelAttribute AdminEventParam params
    ) {
        log.info("GET /admin/events c параметрами: " +
                "users={}, states={}, categories={}, rangeStart={}, rangeEnd={}, from={}, size={}",
                params.getUsers(), params.getStates(), params.getCategories(), params.getRangeStart(),
                params.getRangeEnd(), params.getFrom(), params.getSize());

        return service.getFullEvents(params);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateEvents(
            @PathVariable(name = "eventId") @Positive Long eventId,
            @RequestBody @Valid UpdateEventAdminRequest dto
    ) {
        log.info("PATCH /admin/events/{} с телом: {}", eventId, dto);

        return service.updateEventByAdmin(eventId, dto);
    }
}
