package ru.practicum.stats.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.dto.response.HitsCounterResponseDto;
import ru.practicum.stats.error.ValidationException;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class StatsController {
    private final StatsService statsService;

    @PostMapping("/hit")
    public ResponseEntity<Void> saveHit(
            @RequestBody StatHitRequestDto dto
    ) {
        statsService.saveHit(dto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/stats")
    public ResponseEntity<List<HitsCounterResponseDto>> getStats(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique
    ) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startDate = LocalDateTime.parse(start, dtf);
        LocalDateTime endDate = LocalDateTime.parse(end, dtf);

        if (startDate.isAfter(endDate)) {
            throw new ValidationException("Start date must be before end date");
        }

        List<HitsCounterResponseDto> stats = statsService.getStats(startDate, endDate, uris, unique);

        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }
}
