package ru.practicum.service.admin_ewm.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.admin_ewm.dto.AdminEventParam;
import ru.practicum.service.admin_ewm.service.AdminEventService;
import ru.practicum.service.dto.EventFullDto;
import ru.practicum.service.dto.UpdateEventAdminRequest;

import java.util.List;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class AdminEventController {
    private final AdminEventService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
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
    @ResponseStatus(HttpStatus.OK)
    public EventFullDto updateEvents(
            @PathVariable(name = "eventId") @Positive Long eventId,
            @RequestBody @Valid UpdateEventAdminRequest dto
    ) {
        log.info("PATCH /admin/events/{} с телом: {}", eventId, dto);

        return service.updateEventByAdmin(eventId, dto);
    }
}
