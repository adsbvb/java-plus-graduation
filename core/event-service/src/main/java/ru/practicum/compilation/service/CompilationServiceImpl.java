package ru.practicum.compilation.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.UserClient;
import ru.practicum.client.analyzer.AnalyzerGrpcClient;
import ru.practicum.client.stats_server.StatClient;
import ru.practicum.compilation.dal.CompilationRepository;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.dto.*;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final StatClient statClient;
    private final UserClient userClient;
    private final AnalyzerGrpcClient analyzerGrpcClient;

    @Override
    @Transactional
    public CompilationDto createCompilation(NewCompilationDto dto) {
        if (compilationRepository.existsByTitle(dto.getTitle())) {
            throw new ConflictException("Подборка событий с названием " + dto.getTitle() + " уже существует!");
        }

        Compilation compilation = CompilationMapper.newCompilationToEntity(dto);

        if (dto.getEventsIds() != null && !dto.getEventsIds().isEmpty()) {
            Set<Event> events = new HashSet<>(eventRepository.findAllByIdIn(dto.getEventsIds()));
            if (events.size() != dto.getEventsIds().size()) {
                throw new NotFoundException("Некоторые события не были найдены.");
            }
            compilation.setEvents(events);
        }

        Compilation savedCompilation = compilationRepository.save(compilation);

        Set<EventShortDto> eventShortDtos = getEventShortDto(savedCompilation.getEvents());

        return CompilationMapper.toCompilationDto(savedCompilation, eventShortDtos);
    }

    @Override
    @Transactional
    public void deleteCompilation(Long id) {
        if (!compilationRepository.existsById(id)) {
            throw new NotFoundException("Подборка событий с id=" + id + " не найдена.");
        }
        compilationRepository.deleteById(id);
    }

    @Override
    @Transactional
    public CompilationDto updateCompilation(Long id, UpdateCompilationRequest request) {
        Compilation compilation = compilationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Подборка событий с id=" + id + " не найдена."));

        if (request.getTitle() != null) {
            if (!request.getTitle().equals(compilation.getTitle()) &&
                    compilationRepository.existsByTitle(request.getTitle())) {
                throw new ConflictException("Подборка событий с названием " + request.getTitle() + " уже существует!");
            }
            compilation.setTitle(request.getTitle());
        }

        if (request.getPinned() != null) {
            compilation.setPinned(request.getPinned());
        }

        if (request.getEventsIds() != null) {
            Set<Event> events;
            if (request.getEventsIds().isEmpty()) {
                events = Collections.emptySet();
            } else {
                events = new HashSet<>(eventRepository.findAllByIdIn(request.getEventsIds()));
                if (events.size() != request.getEventsIds().size()) {
                    throw new NotFoundException("Некоторые события не были найдены.");
                }
            }
            compilation.setEvents(events);
        }

        Compilation savedCompilation = compilationRepository.save(compilation);

        Set<EventShortDto> eventShortDtos = getEventShortDto(savedCompilation.getEvents());

        return CompilationMapper.toCompilationDto(savedCompilation, eventShortDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size, HttpServletRequest request) {
        log.info("PublicCompilationService: выгрузка подборок по заданным параметрам");

        List<Compilation> compilationsList = compilationRepository.findCompilations(pinned, from, size);
        if (compilationsList.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Event> allEvents = compilationsList.stream()
                .flatMap(comp -> comp.getEvents().stream())
                .collect(Collectors.toSet());

        if (allEvents.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, EventShortDto> eventShortDtoMap = getEventShortDtoMap(allEvents);

        List<CompilationDto> result = new ArrayList<>();
        for (Compilation comp : compilationsList) {
            Set<EventShortDto> eventDtos = comp.getEvents().stream()
                    .map(Event::getId)
                    .filter(eventShortDtoMap::containsKey)
                    .map(eventShortDtoMap::get)
                    .collect(Collectors.toSet());

            result.add(CompilationMapper.toCompilationDto(comp, eventDtos));
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId, HttpServletRequest request) {
        log.info("PublicCompilationService: поиск подборки с id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Подборка с id: %d не найдена", compId)));

        Set<EventShortDto> eventShortDtoList = getEventShortDto(compilation.getEvents());

        statClient.hit(new StatHitRequestDto(Constant.SERVICE_POSTFIX,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)))
        );

        return CompilationMapper.toCompilationDto(compilation, eventShortDtoList);
    }

    // Private

    private Map<Long, EventShortDto> getEventShortDtoMap(Set<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();

        Map<Long, UserShortDto> userMap = getUsersMap(initiatorIds);

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .toList();

        Map<Long, Double> ratingMap = getRatingsFromAnalyzer(eventIds);

        return events.stream()
                .map(event -> {
                    UserShortDto user = userMap.get(event.getInitiatorId());
                    if (user == null) {
                        log.warn("Инициатор не найден для события {}", event.getId());
                        return null;
                    }
                    Double rating = ratingMap.getOrDefault(event.getId(), 0.0);
                    EventShortDto eventShortDto = EventMapper.toEventShortDto(event, user, rating);
                    return new AbstractMap.SimpleEntry<>(event.getId(), eventShortDto);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Set<EventShortDto> getEventShortDto(Set<Event> events) {
        if (events == null || events.isEmpty()) {
            return Collections.emptySet();
        }

        Map<Long, EventShortDto> eventShortDtoMap = getEventShortDtoMap(events);
        return new HashSet<>(eventShortDtoMap.values());
    }

    private Map<Long, UserShortDto> getUsersMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<UserShortDto> users = userClient.getUsersInternal(userIds);
            if (users == null || users.isEmpty()) {
                log.warn("Не удалось получить пользователей для ID: {}", userIds);
                return Collections.emptyMap();
            }
            return users.stream()
                    .collect(Collectors.toMap(UserShortDto::getId, u -> u));
        } catch (FeignException ex) {
            log.error("Ошибка при вызове сервиса пользователей: {}", ex.getMessage());
            throw new ServiceException("Ошибка при вызове сервиса пользователей", ex);
        }
    }

    private Map<Long, Double> getRatingsFromAnalyzer(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            List<RecommendedEventProto> ratings = analyzerGrpcClient.getInteractionsCount(eventIds);
            return ratings.stream()
                    .collect(Collectors.toMap(
                            RecommendedEventProto::getEventId,
                            RecommendedEventProto::getScore,
                            (v1, v2) -> v1
                    ));
        } catch (Exception e) {
            log.error("Ошибка при получении рейтингов из Analyzer: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}