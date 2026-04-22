package ru.practicum.service.public_ewm.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.service.dto.CompilationDto;
import ru.practicum.service.public_ewm.service.PublicCompilationService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/compilations")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class PublicCompilationController {
    PublicCompilationService compilationService;

    @GetMapping
    public List<CompilationDto> getCompilationsByParam(@RequestParam(value = "pinned", required = false) Boolean pinned,
                                                       @RequestParam(value = "from", defaultValue = "0") Integer from,
                                                       @RequestParam(value = "size", defaultValue = "10") Integer size,
                                                       HttpServletRequest request) {
        log.info("PublicCompilationController: вызов эндпоинта GET /compilation с параметрами --" +
                " pinned: {}, from: {}, size: {}", pinned, from, size);

        return compilationService.getCompilations(pinned, from, size, request);
    }

    @GetMapping("/{compId}")
    public CompilationDto getCompilationById(@PathVariable(value = "compId") Long compId,
                                             HttpServletRequest request) {
        log.info("PublicCompilationController: вызов эндпоинта GET /compilation/{}", compId);

        return compilationService.getCompilationById(compId, request);
    }
}
