package ru.practicum.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@Slf4j
public class StatsClientConfig {

    @Value("${stats.service.url:http://stats-server:9090}")
    private String statsServiceUrl;

    @Bean
    public RestClient restClient() {
        log.info("Настройка RestClient для статистики по URL: {}", statsServiceUrl);
        return RestClient.builder()
                .baseUrl(statsServiceUrl)
                .defaultHeader("User-Agent", "Stats-Client/1.0")
                .build();
    }
}
