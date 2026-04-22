package ru.practicum.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import ru.practicum.service.model.enums.Status;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ParticipationRequestDto {
    // Заявка на участие в событии
    LocalDateTime created; // в swagger формат 2022-09-06T21:10:05.432

    @JsonProperty(value = "event")
    Long eventId;

    Long id;

    @JsonProperty(value = "requester")
    Long requesterId;

    Status status;
}
