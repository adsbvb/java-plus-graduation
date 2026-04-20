package ru.practicum.service.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.service.dto.ParticipationRequestDto;
import ru.practicum.service.model.Request;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@UtilityClass
public class RequestMapper {
    public ParticipationRequestDto toRequestDto(Request request) {
        LocalDateTime created = request.getCreated()
                .truncatedTo(ChronoUnit.MILLIS);

        return ParticipationRequestDto.builder()
                .requesterId(request.getRequester().getId())
                .eventId(request.getEvent().getId())
                .created(created)
                .status(request.getStatus())
                .id(request.getId())
                .build();
    }
}
