package ru.practicum.service.private_ewm.service;

import ru.practicum.service.dto.*;

import java.util.List;

public interface PrivateService {

    List<EventShortDto> getEventsByOwner(Long userId, Long from, Long size);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getInfoEvent(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<ParticipationRequestDto> getInfoRequest(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateStatusRequest(Long userId, Long eventId,
                                                       EventRequestStatusUpdateRequest updateRequest);

    List<ParticipationRequestDto> getInfoOnParticipation(Long userId);

    ParticipationRequestDto createRequestForParticipation(Long userId, Long eventId);

    ParticipationRequestDto canceledRequestForParticipation(Long userId, Long requestId);

}
