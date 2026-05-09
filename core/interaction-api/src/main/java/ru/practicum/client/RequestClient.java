package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service", path = "/internal/requests")
public interface RequestClient {

    @GetMapping("/event/{eventId}")
    List<ParticipationRequestDto> getRequestsByEventIdInternal(
            @PathVariable(name = "eventId") Long eventId);

    @GetMapping("/by-ids")
    List<ParticipationRequestDto> getRequestsByIdsInternal(
            @RequestParam("ids") List<Long> ids);

    @PutMapping("/batch")
    List<ParticipationRequestDto> batchUpdateRequestsIternal(
            @RequestBody List<ParticipationRequestDto> requests);
}
