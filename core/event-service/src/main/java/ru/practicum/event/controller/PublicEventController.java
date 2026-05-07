package ru.practicum.event.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.EventSearchParams;
import ru.practicum.dto.EventShortDto;
import ru.practicum.event.service.EventService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/events")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class PublicEventController {
    private final EventService service;

    @GetMapping
    public List<EventShortDto> getEvents(@Valid EventSearchParams params,
                                         HttpServletRequest request) {
        log.info("PublicEventController: вызов эндпоинта GET events/ " +
                 "с параметрами запроса --  " +
                 "text:{}, categories:{}, paid:{}, rangeStart:{}, rangeEnd:{}, onlyAvailable:{}, sort:{}, from:{}, size:{}",
                params.getText(), params.getCategories(), params.getPaid(), params.getRangeStart(), params.getRangeEnd(),
                params.getOnlyAvailable(), params.getSort(), params.getFrom(), params.getSize());

        return service.getEvents(params.getText(),
                params.getCategories(),
                params.getPaid(),
                params.getRangeStart(),
                params.getRangeEnd(),
                params.getOnlyAvailable(),
                params.getSort(),
                params.getFrom(),
                params.getSize(),
                request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable(value = "id") Long id, HttpServletRequest request) {
        log.info("PublicEventController: вызов эндпоинта GET events/{}", id);

        return service.getById(id, request);
    }
}
