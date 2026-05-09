package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.model.Request;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@UtilityClass
public class RequestMapper {
    public ParticipationRequestDto toRequestDto(Request request) {
        LocalDateTime created = request.getCreated()
                .truncatedTo(ChronoUnit.MILLIS);

        return ParticipationRequestDto.builder()
                .requesterId(request.getRequesterId())
                .eventId(request.getEventId())
                .created(created)
                .status(request.getStatus())
                .id(request.getId())
                .build();
    }

    public Request toRequest(ParticipationRequestDto requestDto) {
        return Request.builder()
                .id(requestDto.getId())
                .created(requestDto.getCreated())
                .eventId(requestDto.getEventId())
                .requesterId(requestDto.getRequesterId())
                .status(requestDto.getStatus())
                .build();
    }
}
