package ru.practicum.service.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.service.dto.CompilationDto;
import ru.practicum.service.dto.EventShortDto;
import ru.practicum.service.dto.NewCompilationDto;
import ru.practicum.service.model.Compilation;

import java.util.*;

@UtilityClass
public class CompilationMapper {
    public Compilation newCompilationToEntity(NewCompilationDto dto) {
        return Compilation.builder()
                .title(dto.getTitle())
                .pinned(dto.getPinned())
                .events(new HashSet<>())
                .build();
    }

    public CompilationDto toCompilationDto(Compilation compilation, Set<EventShortDto> eventsDto) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(eventsDto)
                .build();
    }
}
