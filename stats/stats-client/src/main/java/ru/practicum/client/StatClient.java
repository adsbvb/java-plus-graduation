package ru.practicum.client;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
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
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        return UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(instance.getHost())
                .port(instance.getPort())
                .path(path)
                .build()
                .toUri();
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
        ServiceInstance instance = retryTemplate.execute(cxt -> getInstance());

        UriComponentsBuilder builder = UriComponentsBuilder.newInstance()
                .scheme("http")
                .host(instance.getHost())
                .port(instance.getPort())
                .path(STATS_ENDPOINT)
                .queryParam("start", start.format(FORMATTER))
                .queryParam("end", end.format(FORMATTER))
                .queryParam("unique", unique);

        if (uris != null && !uris.isEmpty()) {
            builder.queryParam("uris", String.join(",", uris));
        }

        URI uri = builder.build().encode().toUri();

        return restClient.get()
                .uri(uri)
                .retrieve()
                .body(new ParameterizedTypeReference<List<HitsCounterResponseDto>>() {});
    }

    public List<HitsCounterResponseDto> getHits(
            List<String> uris,
            Boolean unique
    ) {
        return getHits(VERY_PAST, LocalDateTime.now(), uris, unique);
    }
}