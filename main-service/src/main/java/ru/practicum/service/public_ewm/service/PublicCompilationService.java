package ru.practicum.service.public_ewm.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.service.dto.CompilationDto;

import java.util.List;

public interface PublicCompilationService {
    List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size, HttpServletRequest request);

    CompilationDto getCompilationById(Long compId, HttpServletRequest request);
}
