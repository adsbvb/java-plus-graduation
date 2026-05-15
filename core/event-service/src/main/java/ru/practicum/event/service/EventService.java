package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.*;
import ru.practicum.enums.EventSort;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventFullDto> getFullEvents(AdminEventParam params);

    EventFullDto updateEventByAdmin(Long id, UpdateEventAdminRequest request);

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

    EventFullDto getById(Long eventId, Long userId);

    List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getInfoEvent(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getInfoRequest(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest updateRequest);

    EventFullDto getEventByIdIternal(Long eventId);

    void incrementConfirmedRequestsInternal(Long eventId, int count);

    List<EventShortDto> getRecommendationsForUser(Long userId, int maxResults);

    void likeEvent(Long eventId, Long userId);
}