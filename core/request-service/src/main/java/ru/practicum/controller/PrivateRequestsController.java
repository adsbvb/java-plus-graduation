package ru.practicum.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ParticipationRequestDto;
import ru.practicum.service.RequestService;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/users/{userId}/requests")
public class PrivateRequestsController {
    private final RequestService service;

    @GetMapping
    public ResponseEntity<List<ParticipationRequestDto>> getInfoOnParticipation(
            @PathVariable(value = "userId") Long userId) {
        return ResponseEntity.ok().body(service.getInfoOnParticipation(userId));
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> createRequestForParticipation(
            @PathVariable(value = "userId") Long userId,
            @RequestParam(value = "eventId") Long eventId,
            HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createRequestForParticipation(userId, eventId));
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> canceledRequestForParticipation(
            @PathVariable(value = "userId") Long userId,
            @PathVariable(value = "requestId") Long requestId) {
        return ResponseEntity.ok().body(service.canceledRequestForParticipation(userId, requestId));
    }
}
