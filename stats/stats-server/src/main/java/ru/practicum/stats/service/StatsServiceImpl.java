package ru.practicum.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.dto.response.HitsCounterResponseDto;
import ru.practicum.stats.dal.StatsRepository;
import ru.practicum.stats.mapper.EndpointHitMapper;
import ru.practicum.stats.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final StatsRepository repository;

    @Override
    @Transactional
    public void saveHit(
            StatHitRequestDto dto
    ) {
        EndpointHit endpoint = EndpointHitMapper.toEndpointHit(dto);
        repository.save(endpoint);
    }

    @Override
    public List<HitsCounterResponseDto> getStats(
            LocalDateTime start,
            LocalDateTime end,
            List<String> uris,
            boolean unique
    ) {
        if (unique) {
            return repository.getStatsUnique(start, end, uris);
        } else {
            return repository.getStats(start, end, uris);
        }
    }
}
