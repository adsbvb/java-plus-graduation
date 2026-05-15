package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.*;
import ru.practicum.enums.EventSort;
import ru.practicum.event.service.handler.AdminEventHandler;
import ru.practicum.event.service.handler.InternalEventService;
import ru.practicum.event.service.handler.PrivateEventHandler;
import ru.practicum.event.service.handler.PublicEventHandler;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class EventServiceFacade implements EventService {
    private final AdminEventHandler adminEventHandler;
    private final PublicEventHandler publicEventHandler;
    private final PrivateEventHandler privateEventHandler;
    private final InternalEventService internalEventService;

    @Override
    public List<EventFullDto> getFullEvents(AdminEventParam params) {
        return adminEventHandler.getFullEvents(params);
    }

    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
       return adminEventHandler.updateEventByAdmin(eventId, request);
    }

    @Override
    public List<EventShortDto> getEvents(String text,
                                         List<Long> categories,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Boolean onlyAvailable,
                                         EventSort sort,
                                         Integer from,
                                         Integer size,
                                         HttpServletRequest request) {
        return publicEventHandler.getEvents(
                text, categories,  paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size, request);
    }

    @Override
    public EventFullDto getById(Long eventId, Long userId) {
        return publicEventHandler.getById(eventId, userId);
    }

    @Override
    public List<EventShortDto> getEventsByOwner(Long userId, Integer from, Integer size) {
        return privateEventHandler.getEventsByOwner(userId, from, size);
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        return privateEventHandler.createEvent(userId, newEventDto);
    }

    @Override
    public EventFullDto getInfoEvent(Long userId, Long eventId) {
        return privateEventHandler.getInfoEvent(userId, eventId);
    }

    @Override
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        return privateEventHandler.updateEvent(userId, eventId, updateEventUserRequest);
    }

    @Override
    public List<ParticipationRequestDto> getInfoRequest(Long userId, Long eventId) {
        return privateEventHandler.getInfoRequest(userId, eventId);
    }

    @Override
    public EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        return privateEventHandler.updateStatusRequest(userId, eventId, updateRequest);
    }

    @Override
    public EventFullDto getEventByIdIternal(Long eventId) {
        return internalEventService.getEventById(eventId);
    }

    @Override
    public void incrementConfirmedRequestsInternal(Long eventId, int count) {
        internalEventService.incrementConfirmedRequests(eventId, count);
    }

    @Override
    public List<EventShortDto> getRecommendationsForUser(Long userId, int maxResults) {
        return publicEventHandler.getRecommendationsForUser(userId, maxResults);
    }

    @Override
    public void likeEvent(Long eventId, Long userId) {
        publicEventHandler.likeEvent(eventId, userId);
    }
}
