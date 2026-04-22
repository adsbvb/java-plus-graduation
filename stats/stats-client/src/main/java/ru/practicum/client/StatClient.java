package ru.practicum.client;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.dto.response.HitsCounterResponseDto;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class StatClient {
    private final DiscoveryClient discoveryClient;
    private final RetryTemplate retryTemplate;
    private final RestClient restClient;

    private final String statsServiceId = "stats-server";
    private static final String HIT_ENDPOINT = "/hit";
    private static final String STATS_ENDPOINT = "/stats";

    private static final LocalDateTime VERY_PAST = LocalDateTime.of(2000, 1, 1, 0, 0);

    public StatClient(DiscoveryClient discoveryClient, RetryTemplate retryTemplate) {
        this.discoveryClient = discoveryClient;
        this.retryTemplate = retryTemplate;
        this.restClient = RestClient.create();
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances(statsServiceId)
                    .getFirst();
        } catch (Exception exception) {
            throw new RuntimeException(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + statsServiceId,
                    exception
            );
        }
    }

    private URI makeUri(String path) {
        ServiceInstance instance = retryTemplate.execute(cxt -> getInstance());
        return URI.create("http://" + instance.getHost() + ":" + instance.getPort() + path);
    }

    public ResponseEntity<Void> hit(StatHitRequestDto dto) {
        URI uri = makeUri(HIT_ENDPOINT);
        return restClient.post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }

    public List<HitsCounterResponseDto> getHits(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            Boolean unique
    ) {
        URI baseUri = makeUri(STATS_ENDPOINT);

        String urisParam = (uris != null && !uris.isEmpty())
                ? String.join(",", uris)
                : null;

        String fullUrl = String.format("http://%s:%d%s?start=%s&end=%s&unique=%s",
                baseUri.getHost(),
                baseUri.getPort(),
                STATS_ENDPOINT,
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                unique);

        if (urisParam != null) {
            fullUrl += "&uris=" + urisParam;
        }

        return restClient.get()
                .uri(fullUrl)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<HitsCounterResponseDto>>() {});
    }

    public List<HitsCounterResponseDto> getHits(
            List<String> uris,
            Boolean unique
    ) {
        LocalDateTime start = VERY_PAST;
        LocalDateTime end = LocalDateTime.now();

        String urisParam = (uris != null && !uris.isEmpty())
                ? String.join(",", uris)
                : null;

        URI baseUri = makeUri(STATS_ENDPOINT);

        String fullUrl = String.format("http://%s:%d%s?start=%s&end=%s&unique=%s",
                baseUri.getHost(),
                baseUri.getPort(),
                STATS_ENDPOINT,
                start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                unique);

        if (urisParam != null) {
            fullUrl += "&uris=" + urisParam;
        }

        return restClient.get()
                .uri(fullUrl)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<HitsCounterResponseDto>>() {});
    }
}
