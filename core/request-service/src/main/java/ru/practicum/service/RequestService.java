package ru.practicum.service;

import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    List<ParticipationRequestDto> getInfoOnParticipation(Long userId);

    ParticipationRequestDto createRequestForParticipation(Long userId, Long eventId);

    ParticipationRequestDto canceledRequestForParticipation(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequestsByEventId(Long eventId);

    List<ParticipationRequestDto> getRequestsByIds(List<Long> userIds);

    List<ParticipationRequestDto> batchUpdateRequests(List<ParticipationRequestDto> requests);
}
