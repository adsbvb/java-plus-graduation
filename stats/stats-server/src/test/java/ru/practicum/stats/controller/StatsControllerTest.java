package ru.practicum.stats.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.dto.response.HitsCounterResponseDto;
import ru.practicum.stats.service.StatsService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsService statsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void saveHit_shouldReturnCreated() throws Exception {
        StatHitRequestDto dto = new StatHitRequestDto("app", "/uri", "127.0.0.1",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        doNothing().when(statsService).saveHit(any(StatHitRequestDto.class));

        mockMvc.perform(MockMvcRequestBuilders.post("/hit")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(MockMvcResultMatchers.status().isCreated());
    }

    @Test
    void getStats_shouldReturnOkWithData() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);
        List<String> uris = List.of("/api/users", "/api/events");

        List<HitsCounterResponseDto> mockData = List.of(
                new HitsCounterResponseDto("app1", "/api/users", 10L),
                new HitsCounterResponseDto("app2", "/api/events", 5L)
        );

        when(statsService.getStats(start, end, uris, false))
                .thenReturn(mockData);

        String startStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        String urisStr = String.join("&uris=", uris);

        mockMvc.perform(MockMvcRequestBuilders.get("/stats")
                        .param("start", startStr)
                        .param("end", endStr)
                        .param("uris", uris.toArray(new String[0]))
                        .param("unique", "false"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].app").value("app1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].uri").value("/api/users"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].hits").value(10));
    }

    @Test
    void getStats_withoutUris_shouldWork() throws Exception {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 12, 31, 23, 59);

        when(statsService.getStats(start, end, null, true))
                .thenReturn(List.of());

        String startStr = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endStr = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        mockMvc.perform(MockMvcRequestBuilders.get("/stats")
                        .param("start", startStr)
                        .param("end", endStr)
                        .param("unique", "true"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json("[]"));
    }
}