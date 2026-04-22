package ru.practicum.service.public_ewm.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.client.StatClient;
import ru.practicum.dto.request.StatHitRequestDto;
import ru.practicum.dto.response.HitsCounterResponseDto;
import ru.practicum.service.dal.EventRepository;
import ru.practicum.service.dto.Constant;
import ru.practicum.service.dto.EventFullDto;
import ru.practicum.service.dto.EventShortDto;
import ru.practicum.service.dto.EventSort;
import ru.practicum.service.error.NotFoundException;
import ru.practicum.service.mapper.EventMapper;
import ru.practicum.service.model.Event;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Slf4j
public class PublicEventServiceImpl implements PublicEventService {
    final EventRepository eventRepository;
    final StatClient statClient;
    static final String URI_EVENT_ENDPOINT = "/events/";
    static final LocalDateTime VERY_PAST = LocalDateTime.of(2000, 1, 1, 0, 0);

    @Override
    public List<EventShortDto> getEvents(String text,
                                         List<Long> categories,
                                         Boolean paid,
                                         LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd,
                                         Boolean onlyAvailable,
                                         EventSort sort,
                                         Integer from,
                                         Integer size,
                                         HttpServletRequest request) {
        if (rangeStart == null && rangeEnd == null) rangeStart = LocalDateTime.now();
        if (rangeEnd == null) rangeEnd = LocalDateTime.now().plusYears(1000);

        log.info("PublicEventService: Поиск ивентов с заданными параметрами");
        Pageable pageable = PageRequest.of(from / size, size);
        List<Event> eventsList = eventRepository.findPublicEvents(text, categories, paid, rangeStart, rangeEnd, onlyAvailable, pageable);
        log.info("PublicEventService: {}", eventsList);
        List<String> eventsUrisList = eventsList.stream().map(event -> URI_EVENT_ENDPOINT + event.getId()).toList();

        log.info("PublicEventService: Выгрузка статистики по найденным ивентам");
        List<HitsCounterResponseDto> hitsCounterList = statClient.getHits(rangeStart, rangeEnd, eventsUrisList, false);
        log.info("PublicEventService: {}", hitsCounterList);
        Map<Long, Long> eventIdEventHits =  hitsCounterList.stream()
                .collect(Collectors.toMap(hitsCounter ->
                        EventMapper.extractIdFromUri(hitsCounter.getUri()), HitsCounterResponseDto::getHits));

        List<EventShortDto> result = eventsList.stream()
                .map(event -> EventMapper.toEventShortDto(event, eventIdEventHits.getOrDefault(event.getId(), 0L)))
                .toList();

        if (sort == EventSort.VIEWS) result = result.stream()
                .sorted(Comparator.comparingLong(EventShortDto::getViews)
                        .reversed()).toList();

        statClient.hit(new StatHitRequestDto(Constant.SERVICE_POSTFIX,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)))
        );

        return result;
    }

    @Override
    public EventFullDto getById(Long id, HttpServletRequest request) {
        log.info("PublicEventService: Поиск ивента с переданным id: {}", id);
        Event event = eventRepository.findPublishedById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Событие с id: %d не найдено", id)));

        log.info("PublicEventService: Выгрузка статистики по найденному ивенту");

        statClient.hit(new StatHitRequestDto(Constant.SERVICE_POSTFIX,
                request.getRequestURI(),
                request.getRemoteAddr(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern(Constant.DATE_TIME_FORMAT)))
        );

        List<HitsCounterResponseDto> hitsCounter = statClient.getHits(VERY_PAST,
                LocalDateTime.now(),
                List.of(URI_EVENT_ENDPOINT + event.getId()),
                true);
        Long views = hitsCounter.isEmpty() ? 0L : hitsCounter.getFirst().getHits();

		return EventMapper.toEventFullDto(event, views);
    }


}
