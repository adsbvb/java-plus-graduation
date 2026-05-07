package ru.practicum.compilation.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.EventShortDto;
import ru.practicum.dto.NewCompilationDto;
import ru.practicum.compilation.model.Compilation;

import java.util.HashSet;
import java.util.Set;

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
