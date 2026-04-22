package ru.practicum.service.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class CompilationDto {
    // Подборка событий
    Set<EventShortDto> events;  // // set - uniqueItems: true

    Long id;

    Boolean pinned;

    String title;
}
