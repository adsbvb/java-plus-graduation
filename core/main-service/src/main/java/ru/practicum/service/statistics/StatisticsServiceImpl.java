package ru.practicum.service.statistics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatClient;
import ru.practicum.dto.response.HitsCounterResponseDto;
import ru.practicum.service.dto.EventShortDto;
import ru.practicum.service.mapper.EventMapper;
import ru.practicum.service.model.Event;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {
    private final StatClient statClient;

    private static final String URI_EVENT_ENDPOINT = "/events/";


    @Override
    public Map<String, Long> getViewsByUris(List<String> uris, boolean unique) {
        if (uris == null || uris.isEmpty()) {
            log.debug("Список URI пуст, возвращаем пустую мапу");
            return Collections.emptyMap();
        }

        try {
            List<HitsCounterResponseDto> hits = statClient.getHits(uris, unique);

            if (hits == null || hits.isEmpty()) {
                log.debug("Статистика не найдена для URI: {}", uris);
                return Collections.emptyMap();
            }

            Map<String, Long> result = hits.stream()
                    .collect(Collectors.toMap(
                            HitsCounterResponseDto::getUri,
                            HitsCounterResponseDto::getHits,
                            (existing, replacement) -> existing
                    ));

            log.debug("Получена статистика для {} URI", result.size());
            return result;

        } catch (Exception e) {
            log.error("Ошибка при получении статистики для {} URI: {}", uris.size(), e.getMessage());
            return Collections.emptyMap();
        }
    }

    @Override
    public Long getViewsByUri(String uri, boolean unique) {
        if (uri == null || uri.isBlank()) {
            return 0L;
        }
        Map<String, Long> views = getViewsByUris(List.of(uri), unique);
        return views.getOrDefault(uri, 0L);
    }

    @Override
    public Set<EventShortDto> getEventShortDto(Set<Event> events, boolean unique) {
        List<String> uris = events.stream()
                .map(event -> URI_EVENT_ENDPOINT + event.getId())
                .toList();

        Map<String, Long> eventIdEventHits = getViewsByUris(uris, unique);

        return events.stream()
                .map(event -> {
                    Long views = eventIdEventHits.getOrDefault(URI_EVENT_ENDPOINT + event.getId(), 0L);
                    return EventMapper.toEventShortDto(event, views);
                })
                .collect(Collectors.toSet());
    }
}