package ru.practicum.service.public_ewm.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.service.dto.EventFullDto;
import ru.practicum.service.dto.EventShortDto;
import ru.practicum.service.dto.EventSort;

import java.time.LocalDateTime;
import java.util.List;

public interface PublicEventService {
    List<EventShortDto> getEvents(String text,
                                  List<Long> categories,
                                  Boolean paid,
                                  LocalDateTime rangeStart,
                                  LocalDateTime rangeEnd,
                                  Boolean onlyAvailable,
                                  EventSort sort,
                                  Integer from,
                                  Integer size,
                                  HttpServletRequest request);

    EventFullDto getById(Long id, HttpServletRequest request);
}
