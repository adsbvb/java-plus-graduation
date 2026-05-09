package ru.practicum.compilation.service;

import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.client.StatClient;
import ru.practicum.client.UserClient;
import ru.practicum.dto.*;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.compilation.dal.CompilationRepository;
import ru.practicum.event.dal.EventRepository;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.event.model.Event;
import ru.practicum.statistics.StatisticsService;

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
    private final StatisticsService statisticsService;
    private final StatClient statClient;
    private final UserClient userClient;

    private static final String URI_EVENT_ENDPOINT = "/events/";

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

        Set<EventShortDto> eventShortDtos = getEventShortDto(savedCompilation.getEvents(), false);

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

        Set<EventShortDto> eventShortDtos = getEventShortDto(savedCompilation.getEvents(), false);

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

        Map<Long, EventShortDto> eventShortDtoMap = getEventShortDtoMap(allEvents, false);

        List<CompilationDto> result = new ArrayList<>();
        for (Compilation comp : compilationsList) {
            Set<EventShortDto> eventDtos = comp.getEvents().stream()
                    .map(Event::getId)
                    .filter(eventShortDtoMap::containsKey)
                    .map(eventShortDtoMap::get)
                    .collect(Collectors.toSet());

            result.add(CompilationMapper.toCompilationDto(comp, eventDtos));
        }

        statClient.hit(new StatHitRequestDto(Constant.SERVICE_POSTFIX,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)))
        );

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public CompilationDto getCompilationById(Long compId, HttpServletRequest request) {
        log.info("PublicCompilationService: поиск подборки с id: {}", compId);

        Compilation compilation = compilationRepository.findById(compId)
                .orElseThrow(() -> new NotFoundException(String.format("Подборка с id: %d не найдена", compId)));

        Set<EventShortDto> eventShortDtoList = getEventShortDto(compilation.getEvents(), false);

        statClient.hit(new StatHitRequestDto(Constant.SERVICE_POSTFIX,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)))
        );

        return CompilationMapper.toCompilationDto(compilation, eventShortDtoList);
    }

    private Map<Long, EventShortDto> getEventShortDtoMap(Set<Event> events, boolean unique) {
        if (events == null || events.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> initiatorIds = events.stream()
                .map(Event::getInitiatorId)
                .distinct()
                .toList();

        List<UserShortDto> users;
        try {
            users = userClient.getUsersInternal(initiatorIds);
        } catch (FeignException ex) {
            throw new ServiceException("Ошибка при вызове сервиса пользователей", ex);
        }

        if (users == null || users.isEmpty()) {
            log.warn("Не удалось получить пользователей для событий");
            return Collections.emptyMap();
        }

        Map<Long, UserShortDto> userMap = users.stream()
                .collect(Collectors.toMap(UserShortDto::getId, u -> u));

        List<String> uris = events.stream()
                .map(event -> URI_EVENT_ENDPOINT + event.getId())
                .toList();

        Map<String, Long> viewsByUri = statisticsService.getViewsByUris(uris, unique);

        return events.stream()
                .map(event -> {
                    UserShortDto user = userMap.get(event.getInitiatorId());
                    if (user == null) {
                        log.warn("Инициатор не найден для события {}", event.getId());
                        return null;
                    }
                    Long views = viewsByUri.getOrDefault(URI_EVENT_ENDPOINT + event.getId(), 0L);
                    EventShortDto eventShortDto = EventMapper.toEventShortDtoWithViews(event, user, views);
                    return new AbstractMap.SimpleEntry<>(event.getId(), eventShortDto);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Set<EventShortDto> getEventShortDto(Set<Event> events, boolean unique) {
        if (events == null || events.isEmpty()) {
            return Collections.emptySet();
        }

        Map<Long, EventShortDto> eventShortDtoMap = getEventShortDtoMap(events, unique);
        return new HashSet<>(eventShortDtoMap.values());
    }
}