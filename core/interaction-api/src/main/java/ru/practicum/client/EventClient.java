package ru.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.EventFullDto;
import ru.practicum.dto.UserShortDto;

@FeignClient(name = "event-service", path = "/internal/events")
public interface EventClient {

    @PostMapping("/event/{id}/query")
    EventFullDto getEventInternal(
            @PathVariable(name = "id") Long eventId,
            @RequestBody UserShortDto userShortDto);

    @PutMapping("/event/{eventId}/confirmed/increment")
    void incrementConfirmedRequestsInternal(
            @PathVariable("eventId") Long eventId,
            @RequestParam("count") int count);

}
