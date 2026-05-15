package ru.practicum.client.stats_server;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.dto.response.HitsCounterResponseDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatClient {
    private final RestClient restClient;

    private static final String HIT_ENDPOINT = "/hit";
    private static final String STATS_ENDPOINT = "/stats";
    private static final LocalDateTime VERY_PAST = LocalDateTime.of(2000, 1, 1, 0, 0);

    public ResponseEntity<Void> hit(StatHitRequestDto dto) {
        try {
            log.debug("Отправка hit в stat-service: {}", dto);

            return restClient.post()
                    .uri(HIT_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(dto)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.error("Ошибка при отправке hit в stat-service: {}", e.getMessage());

            return ResponseEntity.ok().build();
        }
    }

    public List<HitsCounterResponseDto> getHits(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique
    ) {
        try {
            String urisParam = (uris != null && !uris.isEmpty())
                    ? String.join(",", uris)
                    : null;

            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(STATS_ENDPOINT)
                            .queryParam("start", start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .queryParam("end", end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .queryParam("unique", unique)
                            .queryParam("uris", urisParam)
                            .build()
                    )
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<HitsCounterResponseDto>>() {});
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage());
            return List.of();
        }
    }

    public List<HitsCounterResponseDto> getHits(
            List<String> uris,
            Boolean unique
    ) {
        try {
            LocalDateTime start = VERY_PAST;
            LocalDateTime end = LocalDateTime.now();

            String urisParam = (uris != null && !uris.isEmpty())
                    ? String.join(",", uris)
                    : null;

            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(STATS_ENDPOINT)
                            .queryParam("start", start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .queryParam("end", end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .queryParam("unique", unique)
                            .queryParam("uris", urisParam)
                            .build()
                    )
                    .retrieve()
                    .body(new org.springframework.core.ParameterizedTypeReference<List<HitsCounterResponseDto>>() {});
        } catch (Exception e) {
            log.error("Ошибка при получении статистики: {}", e.getMessage());
            return List.of();
        }
    }
}