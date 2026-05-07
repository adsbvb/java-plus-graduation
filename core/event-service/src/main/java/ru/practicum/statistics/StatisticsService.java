package ru.practicum.statistics;

import ru.practicum.dto.EventShortDto;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface StatisticsService {
    Map<String, Long> getViewsByUris(List<String> uris, boolean unique);

    Long getViewsByUri(String uri, boolean unique);

   // Set<EventShortDto> getEventShortDto(Set<Event> events, boolean unique);
}
